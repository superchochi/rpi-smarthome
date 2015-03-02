#include <avr/wdt.h>
#include <avr/sleep.h>
#include <avr/power.h>
#include <dht11.h>
#include <SPI.h>
#include "nRF24L01.h"
#include "RF24.h"
#define INTERVAL 60
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

volatile byte counter = 0;
volatile boolean flag = true;

void setup()
{
  MCUSR = 0; //reset the status register of the MCU
  wdt_disable(); //disable the watchdog
  setWdt(); //set up the watchdog
  set_sleep_mode(SLEEP_MODE_PWR_DOWN); //sleep mode - POWER DOWN
  //power
  pinMode(NRF_GND, OUTPUT);
  digitalWrite(NRF_GND, LOW);
  pinMode(NRF_VCC, OUTPUT);
  pinMode(DHT_VCC, OUTPUT);
  digitalWrite(DHT_VCC, HIGH);
  
  Serial.begin(115200);
  fdevopen(&my_putc, 0);
  
  setNrf();
  
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

void setNrf() {
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
  if(flag) {
    power_all_enable();
    digitalWrite(DHT_VCC, HIGH);
    wdt_disable();
    counter = 0;
    flag = false;
    //long time = millis();
    delay(1000);
    setWdt(); //re-set the watchdog
    int chk = DHT11.read(DHT_DATA);
    //Serial.print("Read sensor: ");
    switch (chk)
    {
      case DHTLIB_OK: {
        //Serial.println("OK");
        //delay(10);
        setNrf();
        byte* data = prepareValue((byte) DHT11.temperature, 1);
        boolean sent = writeData(data);
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
  } else {
    if(counter == 0) {
      power_all_disable();
      digitalWrite(NRF_VCC, LOW);
      digitalWrite(DHT_VCC, LOW);
    }
    sleep_mode(); //CPU in sleep-this corresponds to sleep_enable+sleep_cpu+sleep_disable
  }
}

boolean writeData(byte* data) {
  radio.stopListening();
  boolean ok = radio.write(data, RADIO_PAYLOAD);
  radio.startListening();
  delete[] data;
  return ok;
}

void setWdt() {
   SREG &= ~(1<<SREG_I); //disable global interrupts
   //prepare the watchdog's register
   WDTCSR |= ((1<<WDCE) | (1<<WDE));
   //set the "Interrupt Mode" with a timeout of 1 sec
   WDTCSR = ((1<<WDIE)| (1<<WDP2) | (1<<WDP1)); 
   SREG |= (1<<SREG_I); //re-enable global interrupts
}

ISR(WDT_vect) {
   if (++counter >= INTERVAL) { //set here the # of seconds for the timeout
      flag = true;
   }
}

int my_putc( char c, FILE *t) {
  Serial.write( c );
}
