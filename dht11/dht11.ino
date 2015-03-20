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
#include <LowPower.h>
#include <SPI.h>
#include <DHT.h>
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
#define NRF_CE 9
#define NRF_CSN 10

#define DHTTYPE DHT11   // DHT 11

DHT dht(DHT_DATA, DHTTYPE, 3); //threshold 3 works for 8mhz

byte address[] = { 'd', 'e', 'v', '0', '1' };
byte ctrlAddr[] = { '1', '1', '1', '1', '1' };

char tempUid[] = { 't', 'e', 'm', 'p', '1' };
char humiUid[] = { 'h', 'u', 'm', 'i', '1' };

// Set up nRF24L01 radio on SPI bus plus pins 9 & 10
RF24 radio(NRF_CE, NRF_CSN);

// Single radio pipe address for the 2 nodes to communicate.
const uint64_t pipes[2] = { 0x6465763031LL, 0x3131313131LL };//dev01 -> 0x6465763031LL

void setup()
{
  DDRC = B00000000;
  Serial.begin(9600);
  fdevopen(&my_putc, 0);

  initNrf();
  //delay(100);

  //Serial.println("sender started");
  //delay(2000);
  byte* data = pairDevice();
  boolean sent = writeData(data);
  Serial.print("Device add sent: ");
  Serial.println(sent);
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
  //radio.printDetails();
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

byte* pairDevice() {
  byte* data = new byte[RADIO_PAYLOAD];
  int i;
  for (i = 0; i < ADDRESS_LENGTH; i++) {
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
  for (int j = 0; i < k; i++, j++) {
    data[i] = tempUid[j];
  }
  data[i++] = -3;//value type - Byte
  data[i++] = 0;//function value

  data[i++] = -100;//function data
  data[i++] = -101;//function type humidity
  data[i++] = ADDRESS_LENGTH;//uid length
  k = i + ADDRESS_LENGTH;//add uid
  for (int j = 0; i < k; i++, j++) {
    data[i] = humiUid[j];
  }
  data[i++] = -3;//value type - Byte
  data[i++] = 0;//function value
  return data;
}

byte* prepareValue(byte value, byte sensor) {
  byte* data = new byte[RADIO_PAYLOAD];
  int i;
  for (i = 0; i < 5; i++) {
    data[i] = ctrlAddr[i];
    data[i + ADDRESS_LENGTH] = address[i];
  }
  i += ADDRESS_LENGTH;
  data[i++] = -2;
  data[i++] = 1;
  data[i++] = sensor == 1 ? -100 : -101;
  data[i++] = 5;
  int k = i + 5;
  for (int j = 0; i < k; i++, j++) {
    if (sensor == 1) {
      data[i] = tempUid[j];
    } else if (sensor == 2) {
      data[i] = humiUid[j];
    }
  }
  data[i++] = -3;
  data[i++] = value;
  for (; i < RADIO_PAYLOAD; i++) {
    data[i] = 0;
  }
  /*for(int j = 0; j < RADIO_PAYLOAD; j++) {
    Serial.write(data[j]);
  }*/
  return data;
}

void loop()
{
  //long time = millis();
  long vcc = readVcc();
  Serial.println(vcc);
  initDht();
  //delay(1000);
  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  float h = dht.readHumidity();
  // Read temperature as Celsius
  float t = dht.readTemperature();
  deinitDht();

  // Check if any reads failed and exit early (to try again).
  if (isnan(h) || isnan(t)) {
    Serial.println("Failed to read from DHT sensor!");
  } else {
    Serial.print("Temperature: ");
    Serial.println(t);
    Serial.print("Humidity: ");
    Serial.println(h);
    initNrf();
    //delay(100);
    byte* data = prepareValue((byte) t, 1);
    volatile boolean sent = writeData(data);
    Serial.print("Temperature sent: ");
    Serial.println(sent);
    //delay(100);
    data = prepareValue((byte) h, 2);
    sent = writeData(data);
    Serial.print("Humidity sent: ");
    Serial.println(sent);
    deinitNrf();
  }
  delay(100);
  for (int i = 0; i < INTERVAL; i++) {
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

