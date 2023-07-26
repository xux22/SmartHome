CONTIKI_PROJECT = Sensor
all: $(CONTIKI_PROJECT)

CONTIKI=../../..

# Include the MQTT implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/mqtt

-include $(CONTIKI)-Makefile.identify-target
MODULES_REL += arch/platform/$(TARGET)

include $(CONTIKI)/Makefile.include