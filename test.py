# Setup - run once
import machine  # Get the Pin function from the machine module.
from time import sleep # Get the sleep library from the time module.
from machine import Pin, PWM

# This is the built-in green LED on the Pico.
BUILT_IN_LED_PIN = 13

# The line below indicates we are configuring this as an output (not input)
led = machine.Pin(BUILT_IN_LED_PIN, machine.Pin.OUT)
class Servo:
    # these defaults work for the standard TowerPro SG90
    __servo_pwm_freq = 50
    __min_u10_duty = 26 - 0 # offset for correction
    __max_u10_duty = 123- 0  # offset for correction
    min_angle = 0
    max_angle = 180
    current_angle = 0.001


    def __init__(self, pin):
        self.__initialise(pin)


    def update_settings(self, servo_pwm_freq, min_u10_duty, max_u10_duty, min_angle, max_angle, pin):
        self.__servo_pwm_freq = servo_pwm_freq
        self.__min_u10_duty = min_u10_duty
        self.__max_u10_duty = max_u10_duty
        self.min_angle = min_angle
        self.max_angle = max_angle
        self.__initialise(pin)
        


    def move(self, angle):
        # round to 2 decimal places, so we have a chance of reducing unwanted servo adjustments
        angle = round(angle, 2)
        # do we need to move?
        if angle == self.current_angle:
            return
        self.current_angle = angle
        # calculate the new duty cycle and move the motor
        duty_u10 = self.__angle_to_u10_duty(angle)
        self.__motor.duty(duty_u10)

    def __angle_to_u10_duty(self, angle):
        return int((angle - self.min_angle) * self.__angle_conversion_factor) + self.__min_u10_duty


    def __initialise(self, pin):
        self.current_angle = -0.001
        self.__angle_conversion_factor = (self.__max_u10_duty - self.__min_u10_duty) / (self.max_angle - self.min_angle)
        self.__motor = PWM(Pin(pin))
        self.__motor.freq(self.__servo_pwm_freq)
motor=Servo(pin=33) # A changer selon la broche utilisée
# Main loop: Repeat the forever...
while True:
    motor.move(0) # tourne le servo à 0°
    led.value(1)
    sleep(0.5) # leave it on for 1/2 second
    motor.move(90) # tourne le servo à 0°
    led.value(0)
    sleep(1.5) # leave it off for 1/2 second