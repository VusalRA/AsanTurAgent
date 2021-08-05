package com.example.asanturagent.services;

import com.example.asanturagent.configs.RabbitConfig;
import com.example.asanturagent.dtos.AcceptDto;
import com.example.asanturagent.dtos.OfferDto;
import com.example.asanturagent.dtos.RequestDto;
import com.example.asanturagent.enums.Status;
import com.example.asanturagent.exceptions.EmailNotFound;
import com.example.asanturagent.exceptions.OldPasswordIncorrectException;
import com.example.asanturagent.exceptions.RandomNumberIncorrectException;
import com.example.asanturagent.exceptions.RequestException;
import com.example.asanturagent.models.*;
import com.example.asanturagent.registration.token.ConfirmationTokenService;
import com.example.asanturagent.repositories.*;
import com.example.asanturagent.services.email.EmailService;
import com.example.asanturagent.util.CalculateEndDate;
import com.example.asanturagent.util.CreateImage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AgentServiceImpl implements AgentService {

    @Value("${work.starttime}")
    String startTime;

    @Value("${work.endtime}")
    String endTime;

    @Value("${waiting.hours}")
    int waitingHours;

    @Value("${image.path}")
    private String imagePath;

    @Value("${jasper.filename}")
    private String jasperFileName;

    List<Offer> list = new ArrayList<>();

    AgentRepository agentRepo;
    RequestRepository requestRepo;
    AgentRequestRepository agentRequestRepo;
    ArchiveRepository archiveRepo;
    OfferRepository offerRepo;
    ConfirmationTokenService confirmationTokenService;
    ForgotPasswordRepository forgotPasswordRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    EmailService emailService;
    RabbitTemplate rabbitTemplate;

    public AgentServiceImpl(AgentRepository agentRepo, RequestRepository requestRepo, AgentRequestRepository agentRequestRepo, ArchiveRepository archiveRepo, OfferRepository offerRepo, ConfirmationTokenService confirmationTokenService, ForgotPasswordRepository forgotPasswordRepository, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService, RabbitTemplate rabbitTemplate) {
        this.agentRepo = agentRepo;
        this.requestRepo = requestRepo;
        this.agentRequestRepo = agentRequestRepo;
        this.archiveRepo = archiveRepo;
        this.offerRepo = offerRepo;
        this.confirmationTokenService = confirmationTokenService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public List<AgentRequest> getRequests(Long agentId) {
        System.out.println(agentRequestRepo.findAllByAgentIdWithout(agentId).size());
        return agentRequestRepo.findAllByAgentIdWithout(agentId);
    }

    @Override
    public AgentRequest moveToArchive(Long agentId, Long requestId) {
        AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agentId, requestId);
        if (!agentRequest.getStatus().equals(Status.NEW_REQUEST.name())) {
            agentRequest.setArchive(true);
            return agentRequestRepo.save(agentRequest);
        } else {
            throw new RequestException();
        }
    }

    @Override
    public List<AgentRequest> getArchiveList(Long agentId) {
        return agentRequestRepo.findAllByAgentIdAndArchiveTrue(agentId);
    }

//    @Override
//    public List<AgentRequest> getOfferedRequests(Long agentId) {
//        return agentRequestRepo.findAllByAgentId(agentId);
//    }

    public List<AgentRequest> getOfferedRequestsByEmail(String email) {
        Agent agent = agentRepo.findUserByEmail(email);
        return agentRequestRepo.findAllByAgentId(agent.getId());
    }


    public Agent findAgent(String email) {
        return agentRepo.findUserByEmail(email);
    }

    @Override
    public List<AgentRequest> getAcceptRequestsByEmail(String email) {
        Agent agent = agentRepo.findUserByEmail(email);
        return agentRequestRepo.findAllByAgentIdAndAccept(agent.getId());
    }

    @Override
    public List<AgentRequest> getAllRequest(String email, String status) {
        Agent agent = agentRepo.findUserByEmail(email);
        return agentRequestRepo.findAllByAgentIdAndStatus(agent.getId(), status);
    }


    @Override
    public Agent resetPassword(String oldPassword, String newPassword) {
        Agent agent = getAgentFromToken();
        if (bCryptPasswordEncoder.matches(oldPassword, agent.getPassword())) {
            agent.setPassword(bCryptPasswordEncoder.encode(newPassword));
        } else {
            throw new OldPasswordIncorrectException();
        }
        return agentRepo.save(agent);
    }

    @Override
    public Agent forgotPassword(String email) {
        if (!agentRepo.findByEmail(email).isPresent()) {
            throw new EmailNotFound();
        } else {
            int random = new Random().nextInt(900000) + 100000;
            ForgotPassword forgotPassword = new ForgotPassword(email, random);
            forgotPasswordRepository.save(forgotPassword);
            emailService.send(email, String.valueOf(forgotPassword.getRandom()));
            return agentRepo.findUserByEmail(email);
        }

    }

    public void changePassword(String newPassword, Agent agent) {
        agent.setPassword(bCryptPasswordEncoder.encode(newPassword));
        agentRepo.save(agent);
    }

    public String confirm(Integer password) {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByRandom(password).orElseThrow(() -> new RandomNumberIncorrectException());
        return forgotPassword.getEmail();
    }

    @RabbitListener(queues = RabbitConfig.QUEUE2)
    public void listenRequestQueue(RequestDto requestDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        String java = null;
        try {
            java = objectMapper.writeValueAsString(requestDto.getData());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Request request = Request.builder().uuid(requestDto.getUuid()).data(java).requestDateTime(LocalDateTime.now()).requestEndDateTime(CalculateEndDate.getDeadLine(LocalTime.now(), startTime, endTime, waitingHours)).build();
        requestRepo.save(request);
        List<Agent> agents = agentRepo.findAll();
        agents.forEach(appUser -> agentRequestRepo.save(AgentRequest.builder()
                .agentId(appUser.getId()).request(request)
                .status(Status.NEW_REQUEST.name()).archive(false).build()));
    }

    @RabbitListener(queues = RabbitConfig.QUEUE3)
    public void listenAcceptQueue(AcceptDto acceptDto) {
        Request request = requestRepo.findByUuid(acceptDto.getUuid());
        Agent agent = agentRepo.findUserByEmail(acceptDto.getEmail());

        System.out.println(acceptDto.getEmail());
        AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agent.getId(), request.getId());
        agentRequest.setStatus(Status.ACCEPT.name());
        agentRequest.setPhoneNumber(acceptDto.getPhoneNumber());
        agentRequestRepo.save(agentRequest);

    }

    @RabbitListener(queues = RabbitConfig.QUEUE4)
    @Override
    public void listenStopQueue(String uuid) {
        Request request = requestRepo.findByUuid(uuid);
        System.out.println(request.getId());
        List<AgentRequest> agentRequests = agentRequestRepo.findByRequest(request);
        agentRequests.forEach(agentRequest -> {
            if (!agentRequest.getStatus().equals(Status.ACCEPT.name())) {
                agentRequest.setStatus(Status.EXPIRED.name());
                agentRequestRepo.save(agentRequest);
            }
        });
    }

    @Override
    public Agent getAgentFromToken() {
        String tokenPayload = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        String token = tokenPayload.substring(7, tokenPayload.length());
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

    @Scheduled(cron = "0/5 * * * * ?")
    @Override
    public void checkRequestEndTime() {
        List<Request> requests = requestRepo.findAll();
        requests.forEach(request -> {
            if (LocalDateTime.now().isAfter(request.getRequestEndDateTime())) {
                List<AgentRequest> agentRequests = agentRequestRepo.findByRequest(request);
                agentRequests.forEach(agentRequest -> {
                    if (agentRequest.getStatus().equals(Status.NEW_REQUEST.name())) {
                        agentRequest.setStatus(Status.EXPIRED.name());
                        agentRequestRepo.save(agentRequest);
                    }
                });
            }
        });
    }

    @Override
    public Offer createOffer(Offer offer, Long agentId) {
        Agent agent = agentRepo.findById(agentId).get();
        offer.setEmail(agent.getEmail());
        offer.setCompanyName(agent.getCompanyName());

        Request request = requestRepo.findByUuid(offer.getUuid());
        if (agentRequestRepo.existsByAgentIdAndRequestIdAndStatus(agentId, request.getId(), "NEW_REQUEST")) {
            Offer getOffer = offerRepo.save(offer);
            try {
                createJpgWithOffer(getOffer.getId());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (JRException e) {
                e.printStackTrace();
            }
            AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agentId, request.getId());
            agentRequest.setStatus(Status.OFFERED.name());
            agentRequestRepo.save(agentRequest);
        }
        return offer;
    }

    public OfferDto createJpgWithOffer(Long offerId) throws URISyntaxException, JRException {
        URL res = getClass().getClassLoader().getResource(jasperFileName);
        File file = Paths.get(res.toURI()).toFile();
        Offer offer = offerRepo.findById(offerId).get();
        list.add(offer);
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(list);
        Map<String, Object> map = new HashMap<>();
        map.put("createdBy", "Vusal");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, dataSource);
        String path = System.getProperty("user.home") + imagePath;

        CreateImage.createImageWithJasper(path, jasperPrint);

        list.clear();
        OfferDto offerDto = null;
        try {
            offerDto = OfferDto.builder().companyName(offer.getCompanyName()).email(offer.getEmail()).id(offerId).file(Files.readAllBytes(new File(path).toPath())).uuid(offer.getUuid()).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, offerDto);
        return offerDto;
    }

}
