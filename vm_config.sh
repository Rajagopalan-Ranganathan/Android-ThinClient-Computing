#!/bin/bash
set -o verbose
homedir=~
eval homedir=$homedir
echo $homedir

startup_file="$homedir/.vnc/xstartup"
backup_file="$homedir/.vnc/xstartup.bak"
resources_file="$homedir/.Xresources"

# ------------------------
# install tightvnc
# ------------------------
sudo apt install xfce4 xfce4-goodies tightvncserver
# backup the startup file
mv $startup_file $backup_file

# --------------------------------
# configure the vnc startup script
# --------------------------------
echo '#!/bin/bash' > $startup_file
echo 'xrdb $HOME/.Xresources' >> $startup_file
echo 'startxfce4 &' >> $startup_file

# ------------------------
# create vnc service file
# ------------------------
touch vncserver
cat > vncserver <<'EOF'
#!/bin/sh -e
### BEGIN INIT INFO
# Provides:          vncserver
# Required-Start:    networking
# Required-Stop:
# Default-Start:     3 4 5
# Default-Stop:      0 6
### END INIT INFO
PATH="$PATH:/usr/X11R6/bin/"

# The Username:Group that will run VNC
export USER="mccfall2016g10"
#${RUNAS}

# The display that VNC will use
DISPLAY="1"

# Color depth (between 8 and 32)
DEPTH="16"

# The Desktop geometry to use.
#GEOMETRY="<WIDTH>x<HEIGHT>"
#GEOMETRY="800x600"
GEOMETRY="1024x768"
#GEOMETRY="1280x1024"

# The name that the VNC Desktop will have.
NAME="X"

OPTIONS="-name ${NAME} -depth ${DEPTH} -geometry ${GEOMETRY} :${DISPLAY}"

. /lib/lsb/init-functions

case "$1" in
start)
log_action_begin_msg "Starting vncserver for user ${USER} on localhost:${DISPLAY}"
su ${USER} -c "/usr/bin/vncserver"
#vncserver
;;

stop)
log_action_begin_msg "Stoping vncserver for user ${USER} on localhost:${DISPLAY}"
su ${USER} -c "/usr/bin/vncserver -kill :${DISPLAY}"
#vncserver -kill :1
;;

restart)
$0 stop
$0 start
;;
esac

exit 0
EOF
sudo mv vncserver /etc/init.d/vncserver
sudo chmod +x /etc/init.d/vncserver
sudo update-rc.d vncserver defaults
# add executable permissions
sudo chmod +x $startup_file
# create .Xresources file
touch $resources_file

# ------------------------------
# install inkscape or openoffice
# ------------------------------
regexoo='^.*openoffice.*$'
regexink='^.*inkscape.*$'
if [[ $HOSTNAME =~ $regexink ]];then
    # Inkscape
    sudo apt install inkscape
    # inkscape requires a display variable
    # so you can use it from command line
    export DISPLAY=':1'
    # add inkscape to vnc startup script
    echo 'inkscape -g &' >> $startup_file
fi
if [[ $HOSTNAME =~ $regexoo ]];then
    # OpenOffice
    cd $HOME
    # Download
    wget https://sourceforge.net/projects/openofficeorg.mirror/files/4.1.3/binaries/en-US/Apache_OpenOffice_4.1.3_Linux_x86-64_install-deb_en-US.tar.gz
    # Unzip
    tar -xvzf Apache_OpenOffice_4.1.3_Linux_x86-64_install-deb_en-US.tar.gz
    # Install
    cd $HOME/en-US/DEBS
    sudo dpkg -i *.deb
    cd desktop-integration
    sudo dpkg -i *.deb
    cd $HOME
    # Remove unnecessary files
    rm -r en-US
    rm Apache_OpenOffice_4.1.3_Linux_x86-64_install-deb_en-US.tar.gz
    # add openoffice to vnc startup script
    echo 'openoffice4 &' >> $startup_file
fi
# ------------------------
# create a shutdown script
# ------------------------
touch checkvnc.sh
cat > checkvnc.sh <<'EOF'
result=$(cat /proc/uptime | grep -Eo '^[^ ]+')
limit=120.0
var=$(awk 'BEGIN{ print "'$limit'"<"'$result'" }')  
if [ "$var" -eq 1 ];then
ss sport = :5901 | grep ESTAB > /dev/null
if [ $? != 0 ]
then
sudo /sbin/shutdown -P now
fi
fi 
EOF
sudo mv checkvnc.sh /usr/local/bin/checkvnc.sh
sudo chmod 775 /usr/local/bin/checkvnc.sh

# --------------------------------------------------------
# create noVNC websockify proxy and automatic startup file
# --------------------------------------------------------
cd /home/mccfall2016g10/
touch websockify.log
touch websockify.sh
# Download noVNC
git clone git://github.com/kanaka/noVNC
# Create Websockify start script
echo '#!/bin/bash' >> websockify.sh
if [[ $HOSTNAME =~ $regexink ]];then
# Inkscape
echo '/home/mccfall2016g10/noVNC/utils/launch.sh --vnc 146.148.10.18:5901' >> websockify.sh
fi
if [[ $HOSTNAME =~ $regexoo ]];then
# OpenOffice
echo '/home/mccfall2016g10/noVNC/utils/launch.sh --vnc 104.199.44.89:5901' >> websockify.sh
fi
# Move file to proper location
sudo mv websockify.sh /usr/local/bin/websockify.sh
sudo chmod +x /usr/local/bin/websockify.sh

#Set crontabs for websockify and vnc connection check
(crontab -l 2>/dev/null; echo "*/5 * * * * /usr/bin/sudo -H /usr/local/bin/checkvnc.sh >> /dev/null 2>&1") | crontab -
(crontab -l 2>/dev/null; echo "@reboot /usr/bin/sudo -H /usr/local/bin/websockify.sh >> /home/mccfall2016g10/websockify.log 2>&1") | crontab -

# restart server
sudo /etc/init.d/vncserver start

# restart websockify proxy and install dependencies
sudo /usr/local/bin/websockify.sh