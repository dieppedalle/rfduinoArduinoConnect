/*
The sketch demonstrates how to do accept a Bluetooth Low Energy 4
Advertisement connection with the RFduino, then send CPU temperature
updates once a second.

It is suppose to be used with the rfduinoTemperature iPhone application.
*/

/*
 Copyright (c) 2014 OpenSourceRF.com.  All right reserved.

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

#include <RFduinoBLE.h>

void setup() {
  // this is the data we want to appear in the advertisement
  // (if the deviceName and advertisementData are too long to fix into the 31 byte
  // ble advertisement packet, then the advertisementData is truncated first down to
  // a single byte, then it will truncate the deviceName)
  RFduinoBLE.deviceName = "OpenSeat2"; //Sets the device name to RFduino
  RFduinoBLE.customUUID = "00003000-0000-1000-8000-00805f9b34fb";
  //RFduinoBLE.customUUID = "2d63d655-970c-4be7-9deb-3f966537eb2c";
  RFduinoBLE.advertisementData = "temp";
  Serial.begin(9600);
  

  // start the BLE stack
  RFduinoBLE.begin();
}

void loop() {
  // sample once per second
  RFduino_ULPDelay( SECONDS(5) );

  // get a cpu temperature sample
  // degrees c (-198.00 to +260.00)
  // degrees f (-128.00 to +127.00)
  float temp = RFduino_temperature(CELSIUS);

  int sensorValue = analogRead(3);
  float voltage = sensorValue * (3.3 / 1023.0);
  //Serial.println("HELLO");
  //Serial.println(voltage);
  // send the sample to the iPhone
  //Serial.println(voltage);
  //RFduinoBLE.send('5');
  //RFduinoBLE.send('0');
  //RFduinoBLE.send('0');
  //RFduinoBLE.sendFloat(5.0);
  RFduinoBLE.sendFloat(voltage);
}
