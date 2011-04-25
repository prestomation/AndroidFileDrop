#Start of a test for getting a Google Auth token
curl https://www.google.com/accounts/ClientLogin -d Email=$1 -d "Passwd=$2" -d accountType=GOOGLE -d source=Google-cURL-Example -d service=ac2dm | tail -n 1 | sed 's/^.....//g'
