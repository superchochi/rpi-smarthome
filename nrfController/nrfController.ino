#include <SPI.h>
#include "nRF24L01.h"
#include "RF24.h"
#define SERIAL_PAYLOAD 27
#define RADIO_PAYLOAD 32
#define ADDRESS_LENGTH 5
#define NRF_VCC 8
#define NRF_GND 7

byte address[] = {'1','1','1','1','1'};

// Set up nRF24L01 radio on SPI bus plus pins 9 & 10
RF24 radio(9,10);

// Single radio pipe address for the 2 nodes to communicate.
const uint64_t readPipe = 0x3131313131LL;

void setup(void)
{
  //power nrf
  pinMode(2, OUTPUT);
  digitalWrite(2, HIGH);
  pinMode(NRF_GND, OUTPUT);
  pinMode(NRF_VCC, OUTPUT);
  digitalWrite(NRF_GND, LOW);
  digitalWrite(NRF_VCC, HIGH);
  
  Serial.begin(115200);
  fdevopen(&my_putc, 0);
  
  radio.begin();
  radio.setRetries(0,15);
  radio.setAutoAck(true);
  radio.setPayloadSize(RADIO_PAYLOAD);
  radio.openReadingPipe(1,readPipe);
  radio.startListening();
  //radio.printDetails();
  
  //Serial.println("sender started");
}

void loop(void)
{
  if (Serial.available() > 0)
  {
    byte serialData[SERIAL_PAYLOAD];
    byte radioData[RADIO_PAYLOAD];
    byte r = Serial.readBytes((char*)serialData, SERIAL_PAYLOAD);
    if(r == SERIAL_PAYLOAD) {
      addControllerAddressToRequest(radioData, address);
      addDataToRequest(radioData, serialData);
      uint64_t dest = 0;
      for(int i = 0; i < ADDRESS_LENGTH; i++) {
        dest <<= 8;
        dest |= serialData[i];
      }
      radio.stopListening();
      radio.openWritingPipe(dest);
      boolean ok = radio.write(radioData, RADIO_PAYLOAD);
      radio.startListening();
      //Serial.print("sent: ");
      //Serial.println(ok);
      if (ok) {
        // sent
      } else {
        // not sent
      }
    }
  }
  if (radio.available())
  {
    byte radioData[RADIO_PAYLOAD];
    boolean ok = false;
    while(!ok) {
      ok = radio.read(radioData, RADIO_PAYLOAD);
    }
    if(ok && checkAddresses(address, radioData)) {
      for(int i = 0; i < SERIAL_PAYLOAD; i++) {
        Serial.write(radioData[ADDRESS_LENGTH + i]);
      }
      //Serial.println();
    }
  }
}

boolean checkAddresses(byte* ctrlAddr, byte* destAddr) {
  for(int i = 0; i < ADDRESS_LENGTH; i++) {
    if(!(ctrlAddr[i] == destAddr[i])) {
      return false;
    }
  }
  return true;
}

void addControllerAddressToRequest(byte* req, byte* addr) {
  for(int i = 0; i < ADDRESS_LENGTH; i++) {
    req[i] = addr[i];
  }
}

void addDataToRequest(byte* req, byte* data) {
  for(int i = 0; i < SERIAL_PAYLOAD; i++) {
    req[i + ADDRESS_LENGTH] = data[i];
  }
}

int my_putc( char c, FILE *t) {
  Serial.write( c );
}

