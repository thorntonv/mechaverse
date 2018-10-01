package org.mechaverse.manager.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;


@Controller
@ApiIgnore
public class HealthCheck {

    private static final String HEALTHY_JSON = "{\"status\":\"HEALTHY\"}";
    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);


    @RequestMapping(value = "/healthCheck",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"})
    public ResponseEntity<String> healthCheck() {
        try {
            logger.info("Health Check Succeeded");
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(HEALTHY_JSON, responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Health Check Failed", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
