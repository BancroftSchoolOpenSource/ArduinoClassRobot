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
#include <Wire.h>
#include <EasyBNO055_ESP.h>

class Chassis {
public:
	int lCenter = 86;
	int rCenter = 87;
	Servo left;
	Servo right;
	double fwdTarget = 0;
	double rotZTarget = 0;
	double currentRotationZ = 0;
	double rotZIncrement = 1.2;
	double kp = 0.013;
	double fwdConstant = 15;
	double resetTarget = 0;
	int lval;
	int rval;
	Chassis() {
	}
	void setTargets(double fwd, double rotz, double currentRotZ) {
		if (abs(rotz) < 0.01) {
			rotz = 0;
		}
		fwdTarget = fwd;
		rotZTarget += (rotz * rotZIncrement);
		currentRotationZ = currentRotZ;

		write();
	}
	void begin() {
		left.attach(33, 1000, 2000);
		right.attach(32, 1000, 2000);
		left.write(lCenter);
		right.write(rCenter);
	}
	void write() {
		double rotZErr = -kp * (rotZTarget - currentRotationZ);
		lval = fwdConstant * fwdTarget - 90 * rotZErr + lCenter;
		rval = -fwdConstant * fwdTarget - 90 * rotZErr + rCenter;
		if (lval < 0)
			lval = 0;
		if (rval < 0)
			rval = 0;
		if (lval > 180)
			lval = 180;
		if (rval > 180)
			rval = 180;
		left.write(lval);
		right.write(rval);
	}
};

Accessory nunchuck;
EasyBNO055_ESP bno;
Chassis puppy;

void otherI2CUpdate() {
	nunchuck.readData();    // Read inputs and update maps
}

float fmap(float x, float in_min, float in_max, float out_min, float out_max) {
	const float run = in_max - in_min;
	if (run == 0) {
		log_e("map(): Invalid input range, min == max");
		return -1; // AVR returns -1, SAM returns 0
	}
	const float rise = out_max - out_min;
	const float delta = x - in_min;
	return (delta * rise) / run + out_min;
}

// the setup function runs once when you press reset or power the board
void setup() {

	Serial.begin(115200);
	Serial.println("Starting ESP32");
	puppy.begin();
	nunchuck.begin();
	bno.start(&otherI2CUpdate);

}

// the loop function runs over and over again forever
void loop() {

	float x = -fmap(nunchuck.values[1], 0, 255, -1.0, 1.0);
	float y = fmap(nunchuck.values[0], 0, 255, -1.0, 1.0);
	puppy.setTargets(x, y, bno.orientationZ);
	if (nunchuck.values[11] > 0) {
		puppy.rotZTarget = puppy.resetTarget;
	}
	if (nunchuck.values[10] > 0) {
		puppy.resetTarget = puppy.currentRotationZ;
		puppy.rotZTarget = puppy.currentRotationZ;
	}
	delay(10);

	Serial.print("\n\tx= ");
	Serial.print(bno.orientationX);
	Serial.print(" |\ty= ");
	Serial.print(bno.orientationY);
	Serial.print(" |\tz= ");
	Serial.print(bno.orientationZ);
	Serial.print(" |\tL= ");
	Serial.print(puppy.lval);
	Serial.print(" |\tR= ");
	Serial.print(puppy.rval);

}
