# mcc-2016-g10-p1


## Backend

All the files necessary for the deployment and testing of the backend web application can be found under `./backend/`.

### Deployment

 Run the deployment script: `sh runserver.sh`

### Running tests

After a successful deployment, run the testing script: `sh runtests.sh`.

### API Doc

```
POST /api/users/auth
{
	"email" : "...",
	"password" : "..."
}

Response:
Status 200, body:

{ 
	"token" : "..."
}

PUT /api/users/register
{
	"email" : "...",
	"name"  : "....",
	"password" : "..."
}

Response:
Status 201, body:

{ 
	"message" : "Success!"
}

GET /api/users/vms
Required header:	Authorization : Bearer token_from_auth

Response:
Status 200, body:
[
    {
        "name": "..."
        "desc": "...",
        "address": "...",
		"sts": "...",
        "img": "..."
     }
]

POST /api/users/vms/start
Required header:	Authorization : Bearer token_from_auth

{
	"instance" : name_of_instance
}

POST /api/users/vms/stop
Required header:	Authorization : Bearer token_from_auth

{
	"instance" : name_of_instance
}
```

## Frontend

Frontend of the project is implemented both as an Android application and responsive website.

### Android application

To build APK files run buildapks.sh in frontend/android directory.

`sh buildapks.sh`

After the build you can find the APK files at:
multivnc: frontend/android/multivnc/app/build/outputs/apk/app-debug.apk
thin-client-computing: frontend/android/thin-client-computing/app/build/outputs/apk/app-debug.apk

Our Android client uses multivnc as an external VNC app.

### Web application

To build and run web application, run runfront.sh in frontend/web directory.

`sh runfront.sh`

The build script will launch a tunnel to localhost where the webapp will run. This is done to give possibility to run the web app over https for the geolocation to work on mobile.
> Note: As of Chrome 50, the Geolocation API will only work on secure contexts such as HTTPS. If your site is hosted on an non-secure origin (such as HTTP) the requests to get the users location will no longer function.
http://www.w3schools.com/html/html5_geolocation.asp

The website VNC viewer is implemented by using noVNC HTML5 client.

## Virtual Machines

### Requirements

1. Your operating system needs to be a fresh Ubuntu 16.04 LTS
2. In case you use this configuration outside of the specific google cloud project, you need to make specific changes to the script so it reflects your environment.
* You need to alter the lines [156](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh#L156) and [160](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh#L160) so they have right IP address.
* You need to alter the lines [156](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh#L156),[160](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh#L160),[147](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh#L147), [41](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh#L41),[168](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh#L41) so they have right existing username (root/projectname).
3. You must create a firewall rule that allows VNC and Websocket connections to the VM.

### How-to

If you want to configure a fresh Ubuntu 16.04 LTS Virtual Machine, you can use the configuration file ([vm_config.sh](https://git.niksula.hut.fi/cs-e4100/mcc-2016-g10-p1/blob/master/vm_config.sh)). 
You can use SCP or various other methods or just copy paste it into the VM. After the file has been transfered remember to change user rights so you can execute it:
```
chmod +x vm_config.sh
```
Then just execute it:
```
./vm_config.sh
```
VNC IPs are hardcoded in this configuration script. Also, at one point it refers to the google account as an username. This means it wont work without some minor tweaks for other projects.