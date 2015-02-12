#include <dht11.h>
#define DHT11PIN 2

dht11 DHT11;

char ctrlUid[] = { 'c', 't', 'r', 'l', '1' };
char uid[] = { 'r', 'o', 'o', 'm', '1' };
char tempUid[] = { 't', 'e', 'm', 'p', '1' };
char humiUid[] = { 'h', 'u', 'm', 'i', '1' };

void setup()
{
  Serial.begin(115200);
  /*Serial.println("DHT11 TEST PROGRAM ");
  Serial.print("LIBRARY VERSION: ");
  Serial.println(DHT11LIB_VERSION);
  Serial.println();*/
}

void sendValue(byte value, byte sensor) {
  char data[28];
  int i;
  for(i = 0; i < 5; i++) {
    data[i] = uid[i];
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
  for(; i < 27; i++) {
    data[i] = -1;
  }
  data[i] = '\0';
  //Serial.write(data);
  for(int j = 0; j < 27; j++) {
    Serial.write(data[j]);
  }
  //Serial.println();
}

void loop()
{
  int chk = DHT11.read(DHT11PIN);

  //Serial.print("Read sensor: ");
  switch (chk)
  {
    case DHTLIB_OK: 
      //Serial.println("OK"); 
      break;
    case DHTLIB_ERROR_CHECKSUM: 
      //Serial.println("Checksum error"); 
      break;
    case DHTLIB_ERROR_TIMEOUT: 
      //Serial.println("Time out error"); 
      break;
    default: 
      //Serial.println("Unknown error"); 
      break;
  }

  //Serial.print("Humidity (%): ");
  //Serial.println((byte)DHT11.humidity);

  //Serial.print("Temperature (°C): ");
  //Serial.println((byte)DHT11.temperature);
  
  sendValue((byte) DHT11.temperature, 1);

  delay(5000);
}
