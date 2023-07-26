#include "contiki.h"
#include "coap-engine.h"
#include <stdio.h>
#include <string.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "ACTUATOR"
#define LOG_LEVEL LOG_LEVEL_INFO

static bool actuating = false;

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_actuator,
                "title=\"Temperature actuator\" POST mode=on|off, value=up|down;rt=\"Temperature\"",
                NULL,
                res_post_handler,
                NULL,
                NULL
);

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    if(request != NULL)
        LOG_INFO("Post request received");

    size_t len = 0;
    const char *mode = NULL;
    const char *value = NULL;

    len = coap_get_post_variable(request, "mode", &mode);
    if(len > 0 && !actuating && strcmp(mode, "on") == 0){
        len = coap_get_post_variable(request, "value", &value);
        if(strcmp(value, "up")){
            actuating = true;
            LOG_INFO("Actuator turned on to rise the temperature");
        }
        else if(strcmp(value, "down")){
            actuating = true;
            LOG_INFO("Actuator turned on to lower the temperature");
        }
        else
            coap_set_status_code(response, BAD_REQUEST_4_00);
    }else if(len > 0 && actuating && strcmp(mode, "off") == 0){
        actuating = false;
        LOG_INFO("Actuator turned off");
    }else if(len > 0){
        LOG_INFO("Double request");
    }else{
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}