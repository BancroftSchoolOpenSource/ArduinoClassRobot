/*
  Blink

  Turns an LED on for one second, then off for one second, repeatedly.

  Most Arduinos have an on-board LED you can control. On the UNO, MEGA and ZERO
  it is attached to digital pin 13, on MKR1000 on pin 6. LED_BUILTIN is set to
  the correct LED pin independent of which board is used.
  If you want to know what pin the on-board LED is connected to on your Arduino
  model, check the Technical Specs of your board at:
  https://www.arduino.cc/en/Main/Products

  modified 8 May 2014
  by Scott Fitzgerald
  modified 2 Sep 2016
  by Arturo Guadalupi
  modified 8 Sep 2016
  by Colby Newman

  This example code is in the public domain.

  https://www.arduino.cc/en/Tutorial/BuiltInExamples/Blink
*/
#include <ESP32Servo.h>
#include <WiiChuck.h>

Servo left;
Servo right;
Accessory nunchuck;

 float fmap(float x, float in_min, float in_max, float out_min, float out_max) {
    const float run = in_max - in_min;
    if(run == 0){
        log_e("map(): Invalid input range, min == max");
        return -1; // AVR returns -1, SAM returns 0
    }
    const float rise = out_max - out_min;
    const float delta = x - in_min;
    return (delta * rise) / run + out_min;
}
// the setup function runs once when you press reset or power the board
void setup() {
    left.attach(33,1000,2000);
    right.attach(32,1000,2000);
    left.write(90);
    right.write(90);
    Serial.begin(115200);
	  nunchuck.begin();
}

// the loop function runs over and over again forever
void loop() {
  nunchuck.readData();    // Read inputs and update maps

  float x= -fmap(nunchuck.values[1],0,255,-1.0,1.0);
  float y= -fmap(nunchuck.values[0],0,255,-1.0,1.0);
  int lval = 90*x  -90*y   +90;
  int rval = -90*x  -90*y  +92;

  left.write(lval);
  right.write(rval);
}
