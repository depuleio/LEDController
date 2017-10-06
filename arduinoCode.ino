#define REDPIN A0
#define GREENPIN A1
#define BLUEPIN A2
 
#define MAX_millis 2000  

unsigned long starttime;

void setup() {
  // put your setup code here, to run once:
  pinMode(REDPIN, OUTPUT);
  pinMode(GREENPIN, OUTPUT);
  pinMode(BLUEPIN, OUTPUT);
  Serial.begin(9600);


}

void loop() {
  // put your main code here, to run repeatedly:
  int r, g, b;

  

  delay(1000);

  starttime = millis();

  while ( (Serial.available()<3) && ((millis() - starttime) < MAX_millis) )
  {      
     // hang in this loop until we either get 3 bytes of data or 2 second
     // has gone by
  }
  if(Serial.available() < 3)
  {
           // the data didn't come in - handle that problem here
     if(Serial.available() == 0)
     {
        Serial.println("ERROR - Didn't get any bytes of data!");
     }
     else
     {
        Serial.println("Received bytes, but not the right amount");
     }
     
  }
  else
  {
     Serial.println("Got it");
     char byte_array[3] = "";
     for(int n=0; n<3; n++)
     {
       byte_array[n] = Serial.read(); // Then: Get them.
     }
     unsigned char* unsigned_char = reinterpret_cast<unsigned char*>(byte_array);
     r = unsigned_char[0];
     g = unsigned_char[1];
     b = unsigned_char[2];
     Serial.println(r,DEC);
     analogWrite(REDPIN, r);
     analogWrite(GREENPIN, g);
     analogWrite(BLUEPIN, b);
     delay(1000);
  } 
}
