/*
nrf pins: 1(GND):7
          2(VCC):8
          3(CE):9
          4(CSN):10
          5(SCK):13
          6(MOSI):11 ---> power leak, set to input when sleep and back to output when awake
          7(MISO):12
          8(IRQ):empty
*/
#include <stdio.h>
#include <LowPower.h>
#include <SPI.h>
#include <EmonLib.h>
#include "nRF24L01.h"
#include "RF24.h"
#define INTERVAL 15//*4s = 60s
#define DATA_PAYLOAD 27
#define RADIO_PAYLOAD 32
#define ADDRESS_LENGTH 5

#define PACKET_TYPE_PAIR 0x01
#define PACKET_TYPE_UPDATE 0x02

#define FUNCTION_TYPE_METER 0xA4
#define FUNCTION_DATA 0xA0

#define FUNCTION_VALUE_TYPE_DOUBLE 0xB3

#define METER_DATA 1
#define NRF_VCC 8
#define NRF_GND 7
#define NRF_CE 9
#define NRF_CSN 10

//#define IF_SERIAL_DEBUG(x) ({x;})
#define IF_SERIAL_DEBUG(x)

byte address[] = { 'm', 'e', 't', '0', '1' };
byte ctrlAddr[] = { '1', '1', '1', '1', '1' };

char meterUid[] = { 's', 'e', 'n', 's', '1' };

// Set up nRF24L01 radio on SPI bus plus pins 9 & 10
RF24 radio(NRF_CE, NRF_CSN);

EnergyMonitor emon1;// Create an instance

// Single radio pipe address for the 2 nodes to communicate.
const uint64_t pipes[2] = { 0x6d65743031LL, 0x3131313131LL };//met01 -> 0x6d65743031LL

void setup()
{
  DDRC = B00000000;
  IF_SERIAL_DEBUG(Serial.begin(9600));
  IF_SERIAL_DEBUG(fdevopen(&my_putc, 0));

  initNrf();
  IF_SERIAL_DEBUG(radio.printDetails());

  pairDevice();

  emon1.current(1, 60.606);// Current: input pin, calibration(100 / 0.050 / 33(burden))
}

void initNrf() {
  pinMode(NRF_GND, OUTPUT);
  digitalWrite(NRF_GND, LOW);
  pinMode(NRF_VCC, OUTPUT);
  digitalWrite(NRF_VCC, HIGH);
  pinMode(11, OUTPUT);//power leak see top of document
  radio.begin();
  radio.setRetries(0, 15);
  radio.setAutoAck(true);
  radio.setPayloadSize(RADIO_PAYLOAD);
  radio.openWritingPipe(pipes[1]);
  radio.openReadingPipe(1, pipes[0]);
  radio.startListening();
}

void deinitNrf() {
  SPI.end();
  digitalWrite(NRF_VCC, LOW);
  digitalWrite(13, LOW);
  digitalWrite(12, LOW);
  digitalWrite(11, LOW);
  pinMode(11, INPUT);
  digitalWrite(NRF_CSN, LOW);
  digitalWrite(NRF_CE, LOW);
}

void setPacketHeaders(byte* packet, byte type, byte last) {
  int i;
  for (i = 0; i < ADDRESS_LENGTH; i++) {
    packet[i] = ctrlAddr[i];
    packet[i + ADDRESS_LENGTH] = address[i];
  }
  i += ADDRESS_LENGTH;
  packet[i++] = type;
  packet[i] = last;
}

void pairDevice() {
  byte* data = new byte[RADIO_PAYLOAD];
  setPacketHeaders(data, PACKET_TYPE_PAIR, 0);
  int i = (ADDRESS_LENGTH * 2) + 2;
  data[i++] = FUNCTION_DATA;//function data
  data[i++] = FUNCTION_TYPE_METER;//function type meter
  data[i++] = ADDRESS_LENGTH;//uid length
  int k = i + ADDRESS_LENGTH;//add uid
  for (int j = 0; i < k; i++, j++) {
    data[i] = meterUid[j];
  }
  data[i++] = FUNCTION_VALUE_TYPE_DOUBLE;//value type - Double
  for(int j = 0; j < 8; i++, j++) {
    data[i] = 0;//function value
  }
  for (; i < RADIO_PAYLOAD; i++) {
    data[i] = 0;
  }

  writeData(data);

  delete [] data;
}

void updateValue(double value, byte function, char* functionUid, byte valueType) {
  byte* data = new byte[RADIO_PAYLOAD];
  setPacketHeaders(data, PACKET_TYPE_UPDATE, 1);
  int i = (ADDRESS_LENGTH * 2) + 2;
  data[i++] = function;
  data[i++] = ADDRESS_LENGTH;
  int k = i + ADDRESS_LENGTH;
  for (int j = 0; i < k; i++, j++) {
    data[i] = functionUid[j];
  }
  memcpy(data + i, &value, sizeof(double));
  i += sizeof(double);
  for (; i < RADIO_PAYLOAD; i++) {
    data[i] = 0;
  }
  /*for(int j = 0; j < RADIO_PAYLOAD; j++) {
    Serial.write(data[j]);
  }*/
  writeData(data);
  delete [] data;
}

void loop()
{
  double Irms = emon1.calcIrms(1480);  // Calculate Irms only - 1480
  if(Irms < 0.3) {
    Irms = 0;
  }
  double watt = Irms * 232.5;
  //emon1.serialprint();
  IF_SERIAL_DEBUG({
    Serial.print("Watt: ");
    Serial.print(watt);
    Serial.print(" Irms: ");
    Serial.println(Irms);
  });
  updateValue(watt, FUNCTION_TYPE_METER, meterUid, FUNCTION_VALUE_TYPE_DOUBLE);
  IF_SERIAL_DEBUG(delay(100));
  for (int i = 0; i < INTERVAL; i++) {
    LowPower.powerDown(SLEEP_4S, ADC_OFF, BOD_OFF);
  }
}

boolean writeData(byte* data) {
  IF_SERIAL_DEBUG({
    printf("Sending data: ");
    for(int i = 0; i < RADIO_PAYLOAD; i++) {
      printf("%x", data[i]);
    }
    printf("\n");
  });
  radio.stopListening();
  boolean ok = radio.write(data, RADIO_PAYLOAD);
  radio.startListening();
  IF_SERIAL_DEBUG(printf("Data sent: %d\n", ok ? 1 : 0));
  return ok;
}

int my_putc( char c, FILE *t) {
  Serial.write( c );
}

