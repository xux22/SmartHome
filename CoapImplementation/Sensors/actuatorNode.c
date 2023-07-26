#include "contiki.h"
#include <stdio.h>
#include "coap-engine.h"
#include "coap-blocking-api.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "RPL BR"
#define LOG_LEVEL LOG_LEVEL_INFO

#define SERVER_EP "coap://[fd00::1]:5683"

void client_chunk_handler(coap_message_t *response){
    const uint8_t *chunk;
    if(response == NULL){
        LOG_INFO("Request timed out");
        return;
    }

    int len = coap_get_payload(response, &chunk);
    LOG_INFO("|%.*s", len, (char *)chunk);
}

extern coap_resource_t res_actuator;

/* Declare and auto-start this file's process */
PROCESS(actNode, "Actuator node");
AUTOSTART_PROCESSES(&actNode);

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(actNode, ev, data){

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    PROCESS_BEGIN();

    coap_activate_resource(&res_actuator, "act");

    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

    coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
    coap_set_header_uri_path(request, "registration");

    LOG_INFO("Registering to the CoAP server\n");
    COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

    PROCESS_END();
}