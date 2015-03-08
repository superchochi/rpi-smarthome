/*
nrf pins: 1:empty
          2:12
          3:11 ---> power leak, set to input when sleep and back to output when awake
          4:13
          5:10
          6:9
          7:8
          8:7
*/
#include <LowPower.h>
#include <dht11.h>
#include <SPI.h>
#include "nRF24L01.h"
#include "RF24.h"
#define INTERVAL 15//*4s = 60s
#define DATA_PAYLOAD 27
#define RADIO_PAYLOAD 32
#define ADDRESS_LENGTH 5
#define DHT_DATA 2
#define DHT_VCC 3
#define NRF_VCC 8
#define NRF_GND 7

dht11 DHT11;

byte address[] = { 'd', 'e', 'v', '0', '1' };
byte ctrlAddr[] = { '1', '1', '1', '1', '1' };

char tempUid[] = { 't', 'e', 'm', 'p', '1' };
char humiUid[] = { 'h', 'u', 'm', 'i', '1' };

// Set up nRF24L01 radio on SPI bus plus pins 9 & 10
RF24 radio(9,10);

// Single radio pipe address for the 2 nodes to communicate.
const uint64_t pipes[2] = { 0x6465763031LL, 0x3131313131LL };//dev01 -> 0x6465763031LL

void setup()
{
  //power
  pinMode(NRF_GND, OUTPUT);
  digitalWrite(NRF_GND, LOW);
  pinMode(NRF_VCC, OUTPUT);
  pinMode(DHT_VCC, OUTPUT);
  digitalWrite(DHT_VCC, HIGH);
  
  Serial.begin(115200);
  fdevopen(&my_putc, 0);
  
  startNrf();
  
  //Serial.println("sender started");
  //delay(2000);
  byte* data = pairDevice();
  boolean sent = writeData(data);
  Serial.print("Device add sent: ");
  Serial.println(sent);
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

byte* prepareValue(byte value, byte sensor) {
  byte* data = new byte[RADIO_PAYLOAD];
  int i;
  for(i = 0; i < 5; i++) {
    data[i] = ctrlAddr[i];
    data[i + ADDRESS_LENGTH] = address[i];
  }
  i += ADDRESS_LENGTH;
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

void startNrf() {
  pinMode(11, OUTPUT);//power leak see top of document
  digitalWrite(NRF_VCC, HIGH);
  radio.begin();
  radio.setRetries(0,15);
  radio.setAutoAck(true);
  radio.setPayloadSize(RADIO_PAYLOAD);
  radio.openWritingPipe(pipes[1]);
  radio.openReadingPipe(1,pipes[0]);
  radio.startListening();
}

void loop()
{
  //long time = millis();
  digitalWrite(DHT_VCC, HIGH);
  delay(100);
  int chk = DHT11.read(DHT_DATA);
  //Serial.print("Read sensor: ");
  switch (chk)
  {
    case DHTLIB_OK: {
      //Serial.println("OK");
      startNrf();
      byte* data = prepareValue((byte) DHT11.temperature, 1);
      volatile boolean sent = writeData(data);
      Serial.print("Temperature sent: ");
      Serial.println(sent);
      //delay(10);
      data = prepareValue((byte) DHT11.humidity, 2);
      sent = writeData(data);
      Serial.print("Humidity sent: ");
      Serial.println(sent);
      delay(10);
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
  //delay(INTERVAL - (millis() - time));
  digitalWrite(NRF_VCC, LOW);
  digitalWrite(13, LOW);
  digitalWrite(12, LOW);
  pinMode(11, INPUT);
  digitalWrite(10, LOW);
  digitalWrite(9, LOW);
  digitalWrite(DHT_VCC, LOW);
  digitalWrite(DHT_DATA, LOW);
  for(int i = 0; i < INTERVAL; i++) {
    LowPower.powerDown(SLEEP_4S, ADC_OFF, BOD_OFF);
  }
}

boolean writeData(byte* data) {
  radio.stopListening();
  boolean ok = radio.write(data, RADIO_PAYLOAD);
  radio.startListening();
  delete[] data;
  return ok;
}

int my_putc( char c, FILE *t) {
  Serial.write( c );
}
