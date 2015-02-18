#include <dht11.h>
#include <SPI.h>
#include "nRF24L01.h"
#include "RF24.h"
#define DATA_PAYLOAD 27
#define RADIO_PAYLOAD 32
#define ADDRESS_LENGTH 5
#define DHT_DATA 2
#define DHT_VCC 3
#define NRF_VCC 8
#define NRF_GND 7

dht11 DHT11;

byte address[] = { 'd','e','v','0','1' };
byte ctrlAddr[] = { '1', '1', '1', '1', '1' };

char tempUid[] = { 't', 'e', 'm', 'p', '1' };
char humiUid[] = { 'h', 'u', 'm', 'i', '1' };

// Set up nRF24L01 radio on SPI bus plus pins 9 & 10
RF24 radio(9,10);

// Single radio pipe address for the 2 nodes to communicate.
const uint64_t pipes[2] = { 0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL };

void setup()
{
  //power
  pinMode(NRF_GND, OUTPUT);
  digitalWrite(NRF_GND, LOW);
  pinMode(NRF_VCC, OUTPUT);
  digitalWrite(NRF_VCC, HIGH);
  pinMode(DHT_VCC, OUTPUT);
  digitalWrite(DHT_VCC, HIGH);
  
  Serial.begin(115200);
  fdevopen(&my_putc, 0);
  
  radio.begin();
  radio.setRetries(15,15);
  radio.setAutoAck(false);
  radio.setPayloadSize(RADIO_PAYLOAD);
  radio.openWritingPipe(pipes[1]);
  radio.openReadingPipe(1,pipes[0]);
  radio.startListening();
  radio.printDetails();
  
  //Serial.println("sender started");
  byte* data = pairDevice();
  radio.stopListening();
  radio.write(data, RADIO_PAYLOAD);
  radio.startListening();
  delete[] data;
}

byte* pairDevice() {
  byte* data = new byte[RADIO_PAYLOAD];
  int i;
  for(i = 0; i < ADDRESS_LENGTH; i++) {
    data[i] = ctrlAddr[i];
    data[i + ADDRESS_LENGTH] = address[i];
  }
  i += ADDRESS_LENGTH;
  data[i++] = -4;//packet add_device
  data[i++] = 1;//is last packet
  data[i++] = -100;//function data
  data[i++] = -100;//function type temperature
  data[i++] = ADDRESS_LENGTH;//uid length
  int k = i + ADDRESS_LENGTH;//add uid
  for(int j = 0; i < k; i++, j++) {
    data[i] = tempUid[j];
  }
  data[i++] = -3;//value type - Byte
  data[i++] = 0;//function value
  
  data[i++] = -100;//function data
  data[i++] = -101;//function type humidity
  data[i++] = ADDRESS_LENGTH;//uid length
  k = i + ADDRESS_LENGTH;//add uid
  for(int j = 0; i < k; i++, j++) {
    data[i] = humiUid[j];
  }
  data[i++] = -3;//value type - Byte
  data[i++] = 0;//function value
  return data;
}

byte* sendValue(byte value, byte sensor) {
  byte* data = new byte[RADIO_PAYLOAD];
  int i;
  for(i = 0; i < 5; i++) {
    data[i] = ctrlAddr[i];
    data[i + ADDRESS_LENGTH] = address[i];
  }
  data[i++] = -2;
  data[i++] = 1;
  data[i++] = sensor == 1 ? -100 : -101;
  data[i++] = 5;
  int k = i + 5;
  for(int j = 0; i < k; i++, j++) {
    if(sensor == 1) {
      data[i] = tempUid[j];
    } else if(sensor == 2) {
      data[i] = humiUid[j];
    }
  }
  data[i++] = -3;
  data[i++] = value;
  for(; i < RADIO_PAYLOAD; i++) {
    data[i] = 0;
  }
  /*for(int j = 0; j < RADIO_PAYLOAD; j++) {
    Serial.write(data[j]);
  }*/
  return data;
}

void loop()
{
  int chk = DHT11.read(DHT_DATA);
  //Serial.print("Read sensor: ");
  switch (chk)
  {
    case DHTLIB_OK: {
      //Serial.println("OK");
      byte* data = sendValue((byte) DHT11.temperature, 1);
      radio.stopListening();
      radio.write(data, RADIO_PAYLOAD);
      radio.startListening();
      delete[] data;
      data = sendValue((byte) DHT11.humidity, 2);
      radio.stopListening();
      radio.write(data, RADIO_PAYLOAD);
      radio.startListening();
      delete[] data;
      //Serial.println("dht read");
      break;
    }
    case DHTLIB_ERROR_CHECKSUM: {
      //Serial.println("Checksum error");
      break;
    }
    case DHTLIB_ERROR_TIMEOUT: {
      //Serial.println("Time out error");
      break;
    }
    default: {
      //Serial.println("Unknown error");
      break;
    }
  }
  
  delay(10000);
}

int my_putc( char c, FILE *t) {
  Serial.write( c );
}
