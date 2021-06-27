# Power Dict

## What is it

Power-Dict is a dictionary that fetches the word definitions from [Wordnik](https://www.wordnik.com/) REST API.


## Features

+ Maintains a history of valid words that had been searched before

+ Easily remove individual history items or all at once

+ Words in history are stored in cache. Searching for a word in history not hit the network and print cached results



## Dependencies

+ JDK 1.7 or above

+ Maven 3.6.3 or above



## How to use

1. You would require an API from Wordnik to use this program, which you can get for free by signing up [here](https://developer.wordnik.com/).

1. Clone the project on your local machine or extract the downloaded the zip archive

1. If using *nix OS
   
   1. Using the terminal, go to the directory where the project is cloned/extracted

   1. Run the below commands
      ```shell
      chmod +x PowerDict.sh
      ./PowerDict.sh
      ```

1. If using Windows OS

   1. Navigate to the directory where the project is cloned/extracted

   1. Double click on the file `PowerDict.cmd`

1. In the program menu, go to option 5 "Configure API key", then go to option 1 "Add/overwrite the Wordnik API key", and then insert your API key you have received from Step 1.

1. Follow the onscreen instructions for the rest of the program
