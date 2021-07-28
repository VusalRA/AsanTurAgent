package com.example.turaiagent.services;

import com.example.turaiagent.configs.RabbitConfig;
import com.example.turaiagent.dtos.OfferDto;
import com.example.turaiagent.enums.Status;
import com.example.turaiagent.models.*;
import com.example.turaiagent.repositories.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
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

    @Autowired
    RabbitTemplate rabbitTemplate;


    List<Offer> list = new ArrayList<>();

    AgentRepository agentRepo;
    RequestRepository requestRepo;
    AgentRequestRepository agentRequestRepo;
    ArchiveRepository archiveRepo;
    OfferRepository offerRepo;

    public AgentServiceImpl(AgentRepository agentRepo, RequestRepository requestRepo, AgentRequestRepository agentRequestRepo, ArchiveRepository archiveRepo, OfferRepository offerRepo) {
        this.agentRepo = agentRepo;
        this.requestRepo = requestRepo;
        this.agentRequestRepo = agentRequestRepo;
        this.archiveRepo = archiveRepo;
        this.offerRepo = offerRepo;
    }

    @Override
    public Agent registerAgent(Agent agent) {
        return agentRepo.save(agent);
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

    //    @PostConstruct
    public String createJpg(Long offerId) throws URISyntaxException, JRException {
        URL res = getClass().getClassLoader().getResource("data.jrxml");
        File file = Paths.get(res.toURI()).toFile();

        Offer offer = offerRepo.findById(offerId).get();

        list.add(offer);

        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(list);

        Map<String, Object> map = new HashMap<>();
        map.put("createdBy", "Vusal");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, dataSource);
        String path = System.getProperty("user.home") + "\\Desktop\\test.png";
//        JasperExportManager.exportReportToHtmlFile(jasperPrint, path);
//        File file = new File(filePath);
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

    @PostConstruct
    public void plusHour() {
//        int hours = 9;
//
//        String fromWork = "09:00";
//        String toWork = "19:00";
//
//        LocalTime from = LocalTime.of(9, 00, 00);
//        LocalTime to = LocalTime.of(19, 00, 00);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
//
//        if (LocalDateTime.now().format(formatter).compareTo(toWork) != 1) {
//            System.out.println("LocalDate: " + LocalDateTime.now());
//        } else {
//
//        }

//        System.out.println(from);
//        System.out.println(to);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
//
//        String now = LocalDateTime.now().minusHours(3).format(formatter);
//        String plusHours = LocalDateTime.now().plusHours(8).format(formatter);
//
//        System.out.println("NOW: " + now);
//        System.out.println("PLUS 8 Hours: " + plusHours);

    }

    @Override
    public Offer createOffer(Offer offer, Long agentId) {
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

    @RabbitListener(queues = "accept_queue")
    public void listenAccept(Accept accept) throws JsonProcessingException {
//        System.out.println(accept.getCompanyName());
//        String data = Arrays.asList(accept.toString().split("'")).get(1);
//        System.out.println(data);
//        ObjectMapper objectMapper = new ObjectMapper();
//        Accept accept1 = objectMapper.readValue(data, Accept.class);
//        Agent agent = agentRepo.findByCompanyName(accept1.getCompanyName());
        Request request = requestRepo.findByUuid(accept.getUuid());
        Agent agent = agentRepo.findByEmail(accept.getEmail());

        AgentRequest agentRequest = agentRequestRepo.findByAgentIdAndRequestId(agent.getId(), request.getId());
        agentRequest.setStatus(Status.ACCEPT.name());
        agentRequest.setPhoneNumber(accept.getPhoneNumber());
        agentRequestRepo.save(agentRequest);

    }

    //    @PostConstruct
    public String getRequest(String data) throws JsonProcessingException {
        List<Request> requests = requestRepo.findAll();

        ObjectMapper objectMapper = new ObjectMapper();
//
//        requests.forEach(request -> {
        JsonNode node = null;
        try {
            node = objectMapper.readValue(data, JsonNode.class);
//            System.out.println(node.get("select").textValue());
            System.out.println(node.get("uuid").textValue());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return node.get("uuid").textValue();

//        });

//        JsonNode array = objectMapper.readValue(request.getData(), JsonNode.class);
//        System.out.println(array.toString());
//        String request2 = array.get("uuid").textValue();
//        System.out.println(request2);


//        try {
//            RequestTest requestTest = objectMapper.readValue(request.getData(), RequestTest.class);
//            System.out.println(requestTest.getTravellerCount());
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            System.out.println("Error with json");
//        }
    }


    @RabbitListener(queues = "request_queue")
    public void listener(Object request) {
        String data = Arrays.asList(request.toString().split("'")).get(1);
        String uuid = null;
        try {
            uuid = getRequest(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Request request1 = requestRepo.save(Request.builder().uuid(uuid).data(data).requestDateTime(LocalDateTime.now()).build());


        List<Agent> agentList = agentRepo.findAll();
        agentList.forEach(agent -> agentRequestRepo
                .save(AgentRequest.builder()
                        .agentId(agent.getId()).requestId(request1.getId())
                        .status(Status.NEW_REQUEST.name()).build()));
    }

//    @PostConstruct
//    public void test() {
//        Request request1 = requestRepo.findByUuid("d09a8b60-32b0-49e3-ae62-70b269398cb8");
//
//        System.out.println(asd(LocalTime.from(request1.getRequestDateTime())));
//    }

    public static LocalDateTime asd(LocalTime currentTime) {
        String startTime = "09:00";
        String endTime = "19:00";

        LocalDateTime requestDeadline = null;
        if (currentTime.compareTo(LocalTime.parse(startTime)) <= 0) {
            System.out.println("asdasdasd");
            String date = String.valueOf(LocalDate.now());
            String time = String.valueOf(LocalTime.parse(startTime).plusHours(8).plusMinutes(currentTime.getMinute()));
            requestDeadline = LocalDateTime.parse(date + "T" + time);
            return requestDeadline;
        }
        if (currentTime.compareTo(LocalTime.parse(endTime)) >= 0) {
            System.out.println("ghjghjghj");
            String date = String.valueOf(LocalDate.now().plusDays(1));
            String time = String.valueOf(LocalTime.parse(startTime).plusHours(8));
            requestDeadline = LocalDateTime.parse(date + "T" + time);
            return requestDeadline;
        } else {
            int c = 3;
            LocalTime time1 = currentTime;
            while (c != 0) {
                if (!(currentTime.compareTo(LocalTime.parse(endTime)) > 0)) {
                    System.out.println("khgkv");
                    currentTime = currentTime.plusHours(1);
                    requestDeadline = LocalDateTime.parse(String.valueOf(LocalDate.now()) + "T" + String.valueOf(currentTime));
                    if (!(LocalTime.parse(currentTime.getHour() + ":00").compareTo(LocalTime.parse(endTime)) == 0)) {
                        c--;
                    }
                } else {
                    String date = String.valueOf(LocalDate.now().plusDays(1));
                    String time;
                    if ((LocalTime.parse(currentTime.getHour() + ":00").compareTo(LocalTime.parse(endTime)) == 0) && c == 1)
                        time = String.valueOf(LocalTime.parse(startTime).plusHours(0).plusMinutes(currentTime.getMinute()));
                    else
                        time = String.valueOf(LocalTime.parse(startTime).plusHours(c - 1).plusMinutes(currentTime.getMinute()));
                    requestDeadline = LocalDateTime.parse(date + "T" + time);
                    break;
                }
            }
            return requestDeadline;
        }
    }


}
