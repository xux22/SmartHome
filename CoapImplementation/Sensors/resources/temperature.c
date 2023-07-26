#include "contiki.h"
#include "coap-engine.h"
#include "coap-observe.h"
#include <stdio.h>
/* Including the following file to pass the temperature value to the node */
#include "../appVar.h"

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

EVENT_RESOURCE(res_temperature,
                "title=\"Temperature sensor\";obs;rt=\"Temperature\"",
                res_get_handler,
                NULL,
                NULL,
                NULL,
                res_event_handler
);

static void res_event_handler(void){
    coap_notify_observers(&res_temperature);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    unsigned int accept = -1;
    coap_get_header_accept(request, &accept);

    if(accept == -1 || accept == APPLICATION_JSON){
        coap_set_header_content_format(response, APPLICATION_JSON);
        sprintf((char *)buffer, "{\"temperature\": %f, \"timestamp\": %lu}", temperature, clock_seconds());
        coap_set_payload(response, buffer, strlen((char*)buffer));
    }
    else{
        coap_set_status_code(response, BAD_REQUEST_4_00);
        sprintf((char *)buffer, "Message format should be JSON");
        coap_set_payload(response, buffer, strlen((char*)buffer));
    }
}