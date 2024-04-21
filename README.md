# MySurveillance
Surveillance software, for the use of cameras.
At the moment only tested with IPCamera's.

Support for MJPEG and adding support for RTSP

Programmed in Java, tested on Windows 10 & Ubuntu

=================Config=================
 port: The port where the webServer,
       should run on.

 screens: A array of strings,
          A string can be empty for no-camera
          or have the name(Case sensitive) of the camera.

=================Usage=================
  1) java -jar MySurveillance.jar

  3) Program will say there's no,
     config.json and creates one.
     After the program closes for the,
     use to configure the config.
     [See Config]

  4) The program starts & can be accessed,
     over HTTP inside of a browser on the port
     configured inside of the config.

  5) Use the stop command the safely stop,
     the program without losing any settings.

=================Commands=================
  save: Saves config/accounts/cameras

  stop: Saves config/accounts/cameras,
        toggles a boolean to stop all services
        after 15sec forcecloses itself incase any
        while loops are still active.
