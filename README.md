# LostPhone
Takes a picture and sends the location when a keyword SMS is received

Note: This project was split into FL-ASHA for the location part and for the picture part.

The code in the project utilises a broadcast receiver to listen to incoming message; detect for the keyword and activates the services to fetch the current location of the device (using fusedLocationProviderApi) and take a picture from the from camera(using Camera2Api). The fetched location is converted into an address using places API and sent as an SMS. The image taken is saved in memory and sent as an email. The app comes with a very simple UI; a more focus was on the logic implementation.
