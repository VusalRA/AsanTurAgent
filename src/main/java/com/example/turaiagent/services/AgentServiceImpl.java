package com.example.turaiagent.services;

import com.example.turaiagent.configs.RabbitConfig;
import com.example.turaiagent.dtos.*;
import com.example.turaiagent.enums.Status;
import com.example.turaiagent.models.*;
import com.example.turaiagent.registration.token.ConfirmationToken;
import com.example.turaiagent.registration.token.ConfirmationTokenService;
import com.example.turaiagent.repositories.*;
import com.example.turaiagent.services.email.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AgentServiceImpl implements AgentService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${start.time}")
    String startTime;

    @Value("${end.time}")
    String endTime;

    @Value("${waiting.hours}")
    int waitingHours;

    List<Offer> list = new ArrayList<>();

    AgentRepository agentRepo;
    RequestRepository requestRepo;
    AgentRequestRepository agentRequestRepo;
    ArchiveRepository archiveRepo;
    OfferRepository offerRepo;
    ConfirmationTokenService confirmationTokenService;

    public AgentServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder, AgentRepository agentRepo, RequestRepository requestRepo, AgentRequestRepository agentRequestRepo, ArchiveRepository archiveRepo, OfferRepository offerRepo, ConfirmationTokenService confirmationTokenService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.agentRepo = agentRepo;
        this.requestRepo = requestRepo;
        this.agentRequestRepo = agentRequestRepo;
        this.archiveRepo = archiveRepo;
        this.offerRepo = offerRepo;
        this.confirmationTokenService = confirmationTokenService;
    }

    @Override
    public List<AgentRequest> getRequests(Long agentId) {
        System.out.println(agentRequestRepo.findAllByAgentIdWithout(agentId).size());
        return agentRequestRepo.findAllByAgentIdWithout(agentId);
    }

    @Override
    public Archive moveToArchive(Long agentId, Long requestId) {
        AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agentId, requestId);
        if (agentRequest.getStatus().equals(Status.NEW_REQUEST.name())) {
            agentRequest.setStatus(Status.EXPIRED.name());
        }
        Archive archive = archiveRepo.save(new Archive(agentRequest));
        agentRequestRepo.delete(agentRequest);
        return archive;
    }

    @Override
    public List<Archive> getArchiveList(Long agentId) {
        return archiveRepo.findAllByAgentId(agentId);
    }

    @Override
    public List<AgentRequest> getOfferedRequests(Long agentId) {
        return agentRequestRepo.findAllByAgentId(agentId);
    }

    public List<AgentRequest> getOfferedRequestsByEmail(String email) {
        Agent agent = agentRepo.findUserByEmail(email);
        return agentRequestRepo.findAllByAgentId(agent.getId());
    }

    @Override
    public Agent resetPassword(ResetPasswordDto resetPasswordDto) {
        Agent agent = getFromToken();
        if (bCryptPasswordEncoder.matches(resetPasswordDto.getOldPassword(), agent.getPassword())) {
            agent.setPassword(bCryptPasswordEncoder.encode(resetPasswordDto.getNewPassword()));
            System.out.println("Password changed");
            System.out.println(agent.getPassword());
        }
        return agentRepo.save(agent);
    }

    @Override
    public Agent forgotPassword(String email) {

        if (!agentRepo.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email not found");
        } else {
            System.out.println("We found it.");
        }

        int random = new Random().nextInt(900000) + 100000;
        ForgotPassword forgotPassword = new ForgotPassword(email, random);
        forgotPasswordRepository.save(forgotPassword);

        emailService.send(email, String.valueOf(forgotPassword.getRandom()));
        return agentRepo.findUserByEmail(email);
    }

    public Agent findAgent(String email) {
        return agentRepo.findUserByEmail(email);
    }

    @RabbitListener(queues = RabbitConfig.QUEUE4)
    @Override
    public void stopRequest(StopDto stopDto) {

        Request request = requestRepo.findByUuid(stopDto.getUuid());
        AgentRequest agentRequest = agentRequestRepo.findByRequest(request);
        agentRequest.setStatus(Status.EXPIRED.name());
        agentRequestRepo.save(agentRequest);

    }


    //    @Scheduled(cron = "0 0/1 * * * ?")
    @Override
    public void checkRequestEndTime() {
        List<Request> requests = requestRepo.findAll();
        System.out.println(requests.size());
        requests.forEach(request -> {
            if (LocalDateTime.now().isBefore(request.getRequestEndDateTime())) {
                System.out.println("Inside");
                AgentRequest agentRequest = agentRequestRepo.findByRequest(request);
                agentRequest.setStatus(Status.EXPIRED.name());
                agentRequestRepo.save(agentRequest);
            }
        });


    }


    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);

        return "confirmed";
    }

    public String createJpg(Long offerId) throws URISyntaxException, JRException {
        URL res = getClass().getClassLoader().getResource("data.jrxml");
        File file = Paths.get(res.toURI()).toFile();

        Offer offer = offerRepo.findById(offerId).get();
        System.out.println("NOTES JPG: "+offer.getNotes());
        list.add(offer);
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(list);
        Map<String, Object> map = new HashMap<>();
        map.put("createdBy", "Vusal");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, dataSource);
        String path = System.getProperty("user.home") + "\\Desktop\\test.png";
        extractPrintImage(path, jasperPrint);
        list.clear();

        OfferDto offerDto = null;
        try {
            offerDto = OfferDto.builder().companyName(offer.getCompanyName()).email(offer.getEmail()).id(offerId).file(Files.readAllBytes(new File(path).toPath())).uuid(offer.getUuid()).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, offerDto);
        return path;
    }


    private void extractPrintImage(String filePath, JasperPrint print) {
        File file = new File(filePath);
        OutputStream ouputStream = null;
        try {

            ouputStream = new FileOutputStream(file);
            DefaultJasperReportsContext.getInstance();
            JasperPrintManager printManager = JasperPrintManager.getInstance(DefaultJasperReportsContext.getInstance());

            BufferedImage rendered_image = null;
            rendered_image = (BufferedImage) printManager.printPageToImage(print, 0, 1.6f);
            ImageIO.write(rendered_image, "png", ouputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Offer createOffer(Offer offer, Long agentId) {
        System.out.println("NOTE: " + offer.getNotes());
        Agent agent = agentRepo.findById(agentId).get();
        offer.setEmail(agent.getEmail());
        offer.setCompanyName(agent.getCompanyName());

        Request request = requestRepo.findByUuid(offer.getUuid());
        if (agentRequestRepo.existsByAgentIdAndRequestIdAndStatus(agentId, request.getId(), "NEW_REQUEST")) {

            Offer getOffer = offerRepo.save(offer);
            try {
                createJpg(getOffer.getId());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JRException e) {
                e.printStackTrace();
            }
            System.out.println("Success");
            AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agentId, request.getId());
            agentRequest.setStatus(Status.OFFERED.name());
            agentRequestRepo.save(agentRequest);
        }
        return offer;
    }

    @Override
    public Agent getFromToken() {
        String tokenes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        String token = tokenes.substring(7, tokenes.length());
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readValue(payload, JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String email = jsonNode.get("sub").asText();

        return agentRepo.findUserByEmail(email);
    }

    @RabbitListener(queues = "accept_queue")
    public void listenAccept(AcceptDto acceptDto) throws JsonProcessingException {
        Request request = requestRepo.findByUuid(acceptDto.getUuid());
        Agent agent = agentRepo.findUserByEmail(acceptDto.getEmail());

        System.out.println(acceptDto.getEmail());
        AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agent.getId(), request.getId());
        agentRequest.setStatus(Status.ACCEPT.name());
        agentRequest.setPhoneNumber(acceptDto.getPhoneNumber());
        agentRequestRepo.save(agentRequest);

    }

    @RabbitListener(queues = "request_queue")
    public void listener(RequestDto requestDto) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String java = objectMapper.writeValueAsString(requestDto.getData());
        Request request = Request.builder().uuid(requestDto.getUuid()).data(java).requestDateTime(LocalDateTime.now()).requestEndDateTime(endDate(LocalTime.from(LocalDateTime.now()))).build();
        requestRepo.save(request);
        List<Agent> agents = agentRepo.findAll();

//        agents.forEach(appUser -> agentRequestRepo.save(AgentRequest.builder()
//                .agentId(appUser.getId()).requestId(request.getId())
//                .status(Status.NEW_REQUEST.name()).build()));

        agents.forEach(appUser -> agentRequestRepo.save(AgentRequest.builder()
                .agentId(appUser.getId()).request(request)
                .status(Status.NEW_REQUEST.name()).build()));

//        List<Agent> agentList = agentRepo.findAll();
//        agentList.forEach(agent -> agentRequestRepo
//                .save(AgentRequest.builder()
//                        .agentId(agent.getId()).requestId(request.getId())
//                        .status(Status.NEW_REQUEST.name()).build()));
    }


    public LocalDateTime endDate(LocalTime currentTime) {
        LocalDateTime requestDeadline = null;
        if (currentTime.compareTo(LocalTime.parse(startTime)) <= 0) {
            String date = String.valueOf(LocalDate.now());
            String time = String.valueOf(LocalTime.parse(startTime).plusHours(8).plusMinutes(currentTime.getMinute()));
            requestDeadline = LocalDateTime.parse(date + "T" + time);
            return requestDeadline;
        }
        if (currentTime.compareTo(LocalTime.parse(endTime)) >= 0) {
            String date = String.valueOf(LocalDate.now().plusDays(1));
            String time = String.valueOf(LocalTime.parse(startTime).plusHours(8));
            requestDeadline = LocalDateTime.parse(date + "T" + time);
            return requestDeadline;
        } else {

            LocalTime time1 = currentTime;
            while (waitingHours != 0) {
                if (!(currentTime.compareTo(LocalTime.parse(endTime)) > 0)) {
                    currentTime = currentTime.plusHours(1);
                    requestDeadline = LocalDateTime.parse(String.valueOf(LocalDate.now()) + "T" + String.valueOf(currentTime));
                    if (!(LocalTime.parse(currentTime.getHour() + ":00").compareTo(LocalTime.parse(endTime)) == 0)) {
                        waitingHours--;
                    }
                } else {
                    String date = String.valueOf(LocalDate.now().plusDays(1));
                    String time;
                    if ((LocalTime.parse(currentTime.getHour() + ":00").compareTo(LocalTime.parse(endTime)) == 0) && waitingHours == 1)
                        time = String.valueOf(LocalTime.parse(startTime).plusHours(0).plusMinutes(currentTime.getMinute()));
                    else
                        time = String.valueOf(LocalTime.parse(startTime).plusHours(waitingHours - 1).plusMinutes(currentTime.getMinute()));
                    requestDeadline = LocalDateTime.parse(date + "T" + time);
                    break;
                }
            }
            return requestDeadline;
        }
    }

    public void changePassword(String newPassword, Agent agent) {
        System.out.println("NEW PASSWORD: " + newPassword);
        agent.setPassword(bCryptPasswordEncoder.encode(newPassword));
        agentRepo.save(agent);
    }

    public String confirm(Integer password) {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByRandom(password).orElseThrow(() -> new IllegalStateException("random not found"));
        return forgotPassword.getEmail();
    }


    //    public String getRequest(String data) throws JsonProcessingException {
//        List<Request> requests = requestRepo.findAll();
//
//        ObjectMapper objectMapper = new ObjectMapper();
////
////        requests.forEach(request -> {
//        JsonNode node = null;
//        try {
//            node = objectMapper.readValue(data, JsonNode.class);
////            System.out.println(node.get("select").textValue());
//            System.out.println(node.get("uuid").textValue());
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//
//        return node.get("uuid").textValue();
//
//    }


}
