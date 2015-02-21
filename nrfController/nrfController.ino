#include <SPI.h>
#include "nRF24L01.h"
#include "RF24.h"
#define SERIAL_PAYLOAD 27
#define RADIO_PAYLOAD 32
#define ADDRESS_LENGTH 5
#define ACK_SIZE 10

byte ack[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

byte address[] = {'1','1','1','1','1'};

// Set up nRF24L01 radio on SPI bus plus pins 9 & 10
RF24 radio(9,10);

// Single radio pipe address for the 2 nodes to communicate.
const uint64_t pipes[2] = { 0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL };

void setup(void)
{
  //power nrf
  pinMode(2, OUTPUT);
  digitalWrite(2, HIGH);
  
  Serial.begin(115200);
  fdevopen(&my_putc, 0);
  
  radio.begin();
  radio.setRetries(0,15);
  radio.setAutoAck(false);
  radio.setPayloadSize(RADIO_PAYLOAD);
  radio.openWritingPipe(pipes[0]);
  radio.openReadingPipe(1,pipes[1]);
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
      radio.stopListening();
      bool ok = radio.write(radioData, RADIO_PAYLOAD);
      radio.startListening();
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
    bool ok = false;
    while(!ok) {
      ok = radio.read(radioData, RADIO_PAYLOAD);
    }
    if(ok && checkAddresses(address, radioData)) {
      byte dest[ADDRESS_LENGTH];
      for(int i = 0; i < ADDRESS_LENGTH; i++) {
        dest[i] = radioData[i + ADDRESS_LENGTH];
      }
      sendAck(dest);
      for(int i = 0; i < SERIAL_PAYLOAD; i++) {
        Serial.write(radioData[ADDRESS_LENGTH + i]);
      }
      //Serial.println();
    }
  }
}

void sendAck(byte* dest) {
  byte data[RADIO_PAYLOAD];
  addControllerAddressToRequest(data, address);
  int i;
  for(i = 0; i < ADDRESS_LENGTH; i++) {
    data[i + ADDRESS_LENGTH] = dest[i];
  }
  i += ADDRESS_LENGTH;
  for(; i < ACK_SIZE; i++) {
    data[i] = ack[i];
  }
  for(; i < RADIO_PAYLOAD; i++) {
    data[i] = 0;
  }
  radio.stopListening();
  radio.write(data, RADIO_PAYLOAD);
  radio.startListening();
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

