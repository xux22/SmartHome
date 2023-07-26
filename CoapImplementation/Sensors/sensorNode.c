#include "contiki.h"
#include <stdio.h>
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "sys/etimer.h"
#include "node-id.h"
#include "random.h"
/* Including the following file to pass the temperature value to the node */
#include "appVar.h"

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

#define INTERVAL 5 * CLOCK_SECOND
#define MAX_TEMP 25
#define MIN_TEMP 18
#define TARGET_TEMP 21

double temperature = 21.0;
static double temperatureMeasure = 21.0;
extern coap_resource_t res_temperature;
static struct etimer et;
static bool ascending = true;
static bool actuating = false;

/* Declare and auto-start this file's process */
PROCESS(tempNode, "Temperature node");
AUTOSTART_PROCESSES(&tempNode);

void set_ascending(){
    random_init(node_id);
    unsigned short r = random_rand();
    if(r % 2 != 0)
        ascending = false;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(tempNode, ev, data){

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    PROCESS_BEGIN();

    coap_activate_resource(&res_temperature, "temp");

    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

    coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
    coap_set_header_uri_path(request, "registration");

    LOG_INFO("Registering to the CoAP server\n");
    COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

    set_ascending();
    etimer_set(&et, INTERVAL);

	while(1){
		PROCESS_WAIT_EVENT();
		if(ev == PROCESS_EVENT_TIMER && etimer_expired(&et)){
		    if(ascending && !actuating){
		        temperatureMeasure = temperatureMeasure + 0.1;
		        if(temperatureMeasure >= MAX_TEMP)
		            actuating = true;
		    }else if(ascending && actuating){
                temperatureMeasure = temperatureMeasure - 0.1;
                if(temperatureMeasure <= TARGET_TEMP)
                    actuating = false;
		    }else if(!ascending && !actuating){
                temperatureMeasure = temperatureMeasure - 0.1;
                if(temperatureMeasure <= MIN_TEMP)
                    actuating = true;
		    }else{
                temperatureMeasure = temperatureMeasure + 0.1;
                if(temperatureMeasure >= TARGET_TEMP)
                    actuating = false;
		    }
		    temperature = temperatureMeasure;
		    res_temperature.trigger();
		    etimer_reset(&et);
		}
	}

    PROCESS_END();
}