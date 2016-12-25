"use strict";

var apiBaseUrl = "https://mcc.teodorpatras.com/api/";

$(document).ready(function() {	
	if (!localStorage.bearer_token) {
		window.location.href = "/";
	}

	/*jslint white: false */
    /*global window, $, Util, RFB, */

    // Load supporting scripts
    WebUtil.load_scripts({
        'core': ["base64.js", "websock.js", "des.js", "input/keysymdef.js",
                 "input/xtscancodes.js", "input/util.js", "input/devices.js",
                 "display.js", "inflator.js", "rfb.js", "input/keysym.js"],
        'app': ["webutil.js"]});

    var rfb;
    var resizeTimeout;
    var desktopName;


    function UIresize() {
        if (WebUtil.getConfigVar('resize', false)) {
            var innerW = window.innerWidth;
            var innerH = window.innerHeight;
            var controlbarH = document.getElementById('noVNC_status_bar').offsetHeight;
            if (innerW !== undefined && innerH !== undefined)
                rfb.requestDesktopSize(innerW, innerH - controlbarH);
        }
    }
    function FBUComplete(rfb, fbu) {
        UIresize();
        rfb.set_onFBUComplete(function() { });
    }
    function updateDesktopName(rfb, name) {
        desktopName = name;
    }
    function status(text, level) {
        switch (level) {
            case 'normal':
            case 'warn':
            case 'error':
                break;
            default:
                level = "warn";
        }
        document.getElementById('noVNC_status_bar').setAttribute("class", "noVNC_status_" + level);
        document.getElementById('noVNC_status').innerHTML = text;
    }
    function updateState(rfb, state, oldstate) {
        var encrypt = (rfb && rfb.get_encrypt()) ? 'encrypted' : 'unencrypted';
        switch (state) {
            case 'connecting':
                status("Connecting...", "normal");
                break;
            case 'connected':
                status("Connected to: " + localStorage.vm_name.split("-")[2], "normal");
                break;
            case 'disconnecting':
                status("Disconnecting...", "normal");
                break;
            case 'disconnected':
                status("Disconnected", "normal");
                break;
            default:
                status(state, "warn");
                break;
        }

    }
    function disconnected(rfb, reason) {
        if (typeof(reason) !== 'undefined') {
            status(reason, "error");
        }
    }
    function notification(rfb, msg, level, options) {
        status(msg, level);
    }

    window.onresize = function () {
        // When the window has been resized, wait until the size remains
        // the same for 0.5 seconds before sending the request for changing
        // the resolution of the session
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(function(){
            UIresize();
        }, 500);
    };

    window.onscriptsload = function () {
        var host, port, password, path, token;

        WebUtil.init_logging(WebUtil.getConfigVar('logging', 'warn'));
        document.title = unescape(WebUtil.getConfigVar('title', 'VNC Client'));
        // By default, use the host and port of server that served this file
        host = WebUtil.getConfigVar('host', localStorage.vm_address);
        port = WebUtil.getConfigVar('port', "6080");

        // if port == 80 (or 443) then it won't be present and should be
        // set manually
        if (!port) {
            if (window.location.protocol.substring(0,5) == 'https') {
                port = 443;
            }
            else if (window.location.protocol.substring(0,4) == 'http') {
                port = 80;
            }
        }

        password = WebUtil.getConfigVar('password', 'mcc2016');
        path = WebUtil.getConfigVar('path', 'websockify');

        // If a token variable is passed in, set the parameter in a cookie.
        // This is used by nova-novncproxy.
        token = WebUtil.getConfigVar('token', null);
        if (token) {

            // if token is already present in the path we should use it
            path = WebUtil.injectParamIfMissing(path, "token", token);

            WebUtil.createCookie('token', token, 1)
        }

        if ((!host) || (!port)) {
            status('Must specify host and port in URL', 'error');
            return;
        }

        try {
            rfb = new RFB({'target':       document.getElementById('noVNC_canvas'),
                           'encrypt':      WebUtil.getConfigVar('encrypt',
                                    (window.location.protocol === "https:")),
                           'repeaterID':   WebUtil.getConfigVar('repeaterID', ''),
                           'true_color':   WebUtil.getConfigVar('true_color', true),
                           'local_cursor': WebUtil.getConfigVar('cursor', true),
                           'shared':       WebUtil.getConfigVar('shared', true),
                           'view_only':    WebUtil.getConfigVar('view_only', false),
                           'onNotification':  notification,
                           'onUpdateState':  updateState,
                           'onDisconnected': disconnected,
                           'onFBUComplete': FBUComplete});
        } catch (exc) {
            status('Unable to create RFB client -- ' + exc, 'error');
            return; // don't continue trying to connect
        }

        rfb.connect(host, port, password, path);
    };

    // Shut down VM
	$("#terminateVm").click(function() {
		$.ajax({
		    type: "POST",
		    url: apiBaseUrl+"users/vms/stop",
		    contentType: "application/json",
		    dataType: "json",
		    data: JSON.stringify({instance: localStorage.vm_name}),
		    success: function(result) {
		    	window.location.href="/dashboard";
		    },
		    beforeSend: function(xhr, settings) { xhr.setRequestHeader('Authorization','Bearer ' + localStorage.bearer_token)},
		    error: function() {
				console.log("Error stopping VM");
                window.location.href="/dashboard";
		    }
		});
	});

});	