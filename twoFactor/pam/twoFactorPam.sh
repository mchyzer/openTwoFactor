#!/bin/bash


user=$1
pass=$2

# for testing
#if [[ "$user" != "mchyzer" ]]; then
#    exit 0
#fi

# make sure numeric

OTP=`echo "$OTP_INPUT" | tr -c -d 0-9`

if [ $? == 0 ]; then
  COMMAND="/usr/bin/java -jar /home/mchyzer/twoFactor/twoFactorClient.jar --operation=validatePasswordWs --username=$user --twoFacto
rPass=$pass"
  NEW=`$COMMAND`

  if [[ $? == 0 && $NEW == 'true'  ]]; then
    exit 0
  fi
  if [[ $? == 0 && $NEW == 'false'  ]]; then
# pam cant do echos
#    echo "Invalid validation code!, go to http://www.upenn.edu/weblogin/two-step/ for help" > /dev/stderr
#    echo "" > /dev/stderr
    exit 1
  fi
fi

#if there is an error, then dont require two factor
#echo "" > /dev/stderr
#echo "Two-step system is having problems" > /dev/stderr
exit 2
