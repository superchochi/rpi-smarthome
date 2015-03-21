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
#include <DHT.h>
#include "nRF24L01.h"
#include "RF24.h"
#define INTERVAL 15//*4s = 60s
#define DATA_PAYLOAD 27
#define RADIO_PAYLOAD 32
#define ADDRESS_LENGTH 5

#define PACKET_TYPE_PAIR 0x01
#define PACKET_TYPE_UPDATE 0x02
#define PACKET_TYPE_SERIAL 0x03

#define FUNCTION_TYPE_TEMPERATURE 0xA1
#define FUNCTION_TYPE_HUMIDITY 0xA2
#define FUNCTION_TYPE_BATTERY 0xA3
#define FUNCTION_DATA 0xA0

#define FUNCTION_VALUE_TYPE_BYTE 0xB1

#define DHT_DATA 2
#define DHT_VCC 3
#define NRF_VCC 8
#define NRF_GND 7
#define NRF_CE 9
#define NRF_CSN 10

#define DHTTYPE DHT11   // DHT 11

//#define IF_SERIAL_DEBUG(x) ({x;})
#define IF_SERIAL_DEBUG(x)

DHT dht(DHT_DATA, DHTTYPE, 3); //threshold 3 works for 8mhz

byte address[] = { 'd', 'e', 'v', '0', '1' };
byte ctrlAddr[] = { '1', '1', '1', '1', '1' };

char tempUid[] = { 't', 'e', 'm', 'p', '1' };
char humiUid[] = { 'h', 'u', 'm', 'i', '1' };
char battUid[] = { 'b', 'a', 't', 't', '1' };

// Set up nRF24L01 radio on SPI bus plus pins 9 & 10
RF24 radio(NRF_CE, NRF_CSN);

// Single radio pipe address for the 2 nodes to communicate.
const uint64_t pipes[2] = { 0x6465763031LL, 0x3131313131LL };//dev01 -> 0x6465763031LL

void setup()
{
  DDRC = B00000000;
  IF_SERIAL_DEBUG(Serial.begin(9600));
  IF_SERIAL_DEBUG(fdevopen(&my_putc, 0));

  initNrf();
  IF_SERIAL_DEBUG(radio.printDetails());

  pairDevice();
}

void initDht() {
  pinMode(DHT_VCC, OUTPUT);
  digitalWrite(DHT_VCC, HIGH);
  dht.begin();
}

void deinitDht() {
  digitalWrite(DHT_VCC, LOW);
  digitalWrite(DHT_DATA, LOW);
}

void initNrf() {
  pinMode(NRF_GND, OUTPUT);
  digitalWrite(NRF_GND, LOW);
  pinMode(NRF_VCC, OUTPUT);
  digitalWrite(NRF_VCC, HIGH);
  //pinMode(11, OUTPUT);//power leak see top of document
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
  digitalWrite(NRF_CSN, LOW);
  digitalWrite(NRF_CE, LOW);
}

void setPacketHeaders(byte* packet, byte type, byte last) {
  int i;
  for (i = 0; i < ADDRESS_LENGTH; i++) {
    packet[i] = ctrlAddr[i];
    packet[i + ADDRESS_LENGTH] = address[i];
  }
  packet[i++] = type;
  packet[i] = last;
}

void pairDevice() {
  byte* data = new byte[RADIO_PAYLOAD];
  setPacketHeaders(data, PACKET_TYPE_PAIR, 0);
  int i = (ADDRESS_LENGTH * 2) + 2;
  data[i++] = FUNCTION_DATA;//function data
  data[i++] = FUNCTION_TYPE_TEMPERATURE;//function type temperature
  data[i++] = ADDRESS_LENGTH;//uid length
  int k = i + ADDRESS_LENGTH;//add uid
  for (int j = 0; i < k; i++, j++) {
    data[i] = tempUid[j];
  }
  data[i++] = FUNCTION_VALUE_TYPE_BYTE;//value type - Byte
  data[i++] = 0;//function value

  data[i++] = FUNCTION_DATA;//function data
  data[i++] = FUNCTION_TYPE_HUMIDITY;//function type humidity
  data[i++] = ADDRESS_LENGTH;//uid length
  k = i + ADDRESS_LENGTH;//add uid
  for (int j = 0; i < k; i++, j++) {
    data[i] = humiUid[j];
  }
  data[i++] = FUNCTION_VALUE_TYPE_BYTE;//value type - Byte
  data[i++] = 0;//function value

  writeData(data);

  memset(data, 0, RADIO_PAYLOAD);
  setPacketHeaders(data, PACKET_TYPE_SERIAL, 1);
  i = (ADDRESS_LENGTH * 2) + 2;
  data[i++] = FUNCTION_DATA;//function data
  data[i++] = FUNCTION_TYPE_BATTERY;//function type temperature
  data[i++] = ADDRESS_LENGTH;//uid length
  k = i + ADDRESS_LENGTH;//add uid
  for (int j = 0; i < k; i++, j++) {
    data[i] = battUid[j];
  }
  data[i++] = FUNCTION_VALUE_TYPE_BYTE;//value type - Byte
  data[i++] = 0;//function value
  for (; i < RADIO_PAYLOAD; i++) {
    data[i] = 0;
  }

  writeData(data);

  delete [] data;
}

void updateValue(byte value, byte function, char* functionUid, byte valueType) {
  byte* data = new byte[RADIO_PAYLOAD];
  setPacketHeaders(data, PACKET_TYPE_UPDATE, 1);
  int i = (ADDRESS_LENGTH * 2) + 2;
  data[i++] = function;
  data[i++] = ADDRESS_LENGTH;
  int k = i + ADDRESS_LENGTH;
  for (int j = 0; i < k; i++, j++) {
    data[i] = functionUid[j];
  }
  data[i++] = value;
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
  long vcc = readVcc();
  initDht();
  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  float h = dht.readHumidity();
  // Read temperature as Celsius
  float t = dht.readTemperature();
  deinitDht();

  // Check if any reads failed and exit early (to try again).
  if (isnan(h) || isnan(t)) {
    IF_SERIAL_DEBUG(printf("Failed to read from DHT sensor!"));
  } else {
    IF_SERIAL_DEBUG(printf("Temperature: %d\nHumidity: %d\nBattery: %d\n", (int)t, (int)h, (int)vcc));
    initNrf();
    updateValue((byte) t, FUNCTION_TYPE_TEMPERATURE, tempUid, FUNCTION_VALUE_TYPE_BYTE);
    updateValue((byte) h, FUNCTION_TYPE_HUMIDITY, humiUid, FUNCTION_VALUE_TYPE_BYTE);
    updateValue((byte) 84, FUNCTION_TYPE_BATTERY, battUid, FUNCTION_VALUE_TYPE_BYTE);

    deinitNrf();
  }
  IF_SERIAL_DEBUG(delay(100));
  for (int i = 0; i < INTERVAL; i++) {
    LowPower.powerDown(SLEEP_4S, ADC_OFF, BOD_OFF);
  }
}

boolean writeData(byte* data) {
  radio.stopListening();
  boolean ok = radio.write(data, RADIO_PAYLOAD);
  radio.startListening();
  IF_SERIAL_DEBUG(printf("Data sent: %d\n", ok ? 1 : 0));
  return ok;
}

int my_putc( char c, FILE *t) {
  Serial.write( c );
}

long readVcc() {
  // Read 1.1V reference against AVcc
  // set the reference to Vcc and the measurement to the internal 1.1V reference
#if defined(__AVR_ATmega32U4__) || defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
ADMUX = _BV(REFS0) | _BV(MUX4) | _BV(MUX3) | _BV(MUX2) | _BV(MUX1);
#elif defined (__AVR_ATtiny24__) || defined(__AVR_ATtiny44__) || defined(__AVR_ATtiny84__)
ADMUX = _BV(MUX5) | _BV(MUX0);
#elif defined (__AVR_ATtiny25__) || defined(__AVR_ATtiny45__) || defined(__AVR_ATtiny85__)
ADMUX = _BV(MUX3) | _BV(MUX2);
#else
ADMUX = _BV(REFS0) | _BV(MUX3) | _BV(MUX2) | _BV(MUX1);
#endif

  delay(2); // Wait for Vref to settle
  ADCSRA |= _BV(ADSC); // Start conversion
  while (bit_is_set(ADCSRA, ADSC)); // measuring

  uint8_t low  = ADCL; // must read ADCL first - it then locks ADCH
  uint8_t high = ADCH; // unlocks both

  long result = (high << 8) | low;

  result = 1125300L / result; // Calculate Vcc (in mV); 1125300 = 1.1*1023*1000
  return result; // Vcc in millivolts
}

