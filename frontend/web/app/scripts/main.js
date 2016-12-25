var apiBaseUrl = "https://mcc.teodorpatras.com/api/";
var vmList = [];

$(document).ready(function() {
	
	// Initialize materialize selects
	$('select').material_select();

	// Check user logged in
	if (!localStorage.bearer_token && window.location.pathname != "/") {
		window.location.href = "/";
	}

	// In dashboard get get user location
	if (localStorage.bearer_token && window.location.pathname.includes("dashboard")) {
		$("#db-spinner-container").show();
		navigator.geolocation.getCurrentPosition(function (position) {
			loadVmList(position);
		}, function(error) {
			loadVmList(null);
		}, {timeout: 15000});
	}

	// User login
	$( "#login" ).submit(function( event ) {
		event.preventDefault();
		var userInfo = $(this).serializeArray().reduce(function(obj, item) {
		    obj[item.name] = item.value;
		    return obj;
		}, {});
		// On successful post, save bearer token
		$.ajax({
		    type: "POST",
		    url: apiBaseUrl+"users/auth",
		    contentType: "application/json",
		    dataType: "json",
		    data: JSON.stringify(userInfo),
		    success: function(result) {
		    	localStorage.setItem("bearer_token", result.token);
		    	window.location.href = "/dashboard";
		    },
		    error: function() {
		    	$(".login-error").text("Login error");
		    	console.log("Login error");
		    }
		});
	});

	// User register
	$( "#register" ).submit(function( event ) {
		event.preventDefault();
		var userInfo = $(this).serializeArray().reduce(function(obj, item) {
		    obj[item.name] = item.value;
		    return obj;
		}, {});
		// On successful register move to login
		$.ajax({
		    type: "PUT",
		    url: apiBaseUrl+"users/register",
		    dataType: "json",
		    contentType: "application/json",
		    data: JSON.stringify(userInfo),
		    success: function(result) {
		    	window.location.href = "/";
		    }
		});
	});

	// Submit dashboard select
	$( "#dashboard" ).submit(function( event ) {
		event.preventDefault();
		var formInfo = $(this).serializeArray().reduce(function(obj, item) {
		    obj[item.name] = item.value;
		    return obj;
		}, {});
		var vm_name = formInfo.selectedVm;
		var lookup = {};
		var selectedVm;
		// Find selected vm
		for (var i = 0; i < vmList.length; i++) {
		    if (vm_name === vmList[i].name) {
				selectedVm = vmList[i];
		    }
		}
		// Save name and address of vm
		localStorage.setItem("vm_name", selectedVm.name);
		localStorage.setItem("vm_address", selectedVm.address);
		// If vm running, move to vnc client, else start vm by post request
		if (selectedVm.sts == "RUNNING") {
			window.location.href = "/vnc";
		} else {
			$("#db-spinner-container").show();
			$.ajax({
			    type: "POST",
			    url: apiBaseUrl+"users/vms/start",
			    contentType: "application/json",
			    dataType: "json",
			    data: JSON.stringify({instance: selectedVm.name}),
			    success: function(result) {
			    	$("#db-spinner-container").hide();
					window.location.href = "/vnc";
			    },
			    beforeSend: function(xhr, settings) { xhr.setRequestHeader('Authorization','Bearer ' + localStorage.bearer_token)},
			    error: function() {
			    	$("#db-spinner-container").hide();
					console.log("Error starting VM");
					$(".dashboard-error").text("Error starting VM");
			    }
			});
		}
	});

	// Load list of available VMs based on user location
	function loadVmList(position) {
		// Calculate user distance from T-Building
		var userDistance;
		var maxDistance = 50;
		if (position) {
			var tBuildingCoords = {lat: 60.186917, lon: 24.821654};
			var userPosition = {lat: position.coords.latitude, lon: position.coords.longitude};
			userDistance = distanceOfTwoPoints(tBuildingCoords.lon, tBuildingCoords.lat, userPosition.lon, userPosition.lat);
		}
		// Get list of VMs
		$.ajax({
		    type: "GET",
		    url: apiBaseUrl+"users/vms",
		    success: function(result) {
		    	$("#db-spinner-container").hide();
		    	vmList = result;
		    	for (var i=0; i<vmList.length; i++) {
		    		var vmObject = vmList[i];
		    		var vmName;
		    		if (vmObject.name.includes("openoffice")) {
						vmName = "OpenOffice"
		    		} else if (vmObject.name.includes("inkscape")) {
						vmName = "InkScape"
		    		} else {
		    			vmName = vmObject.name.split("-")[2];
		    		}
		    		var optionObj;
		    		var primaryVm;
		    		// Set select options based on user location and VM name
		    		if ((vmObject.name.includes("openoffice") && userDistance && userDistance <= maxDistance) || (!vmObject.name.includes("openoffice") && (!userDistance || userDistance > maxDistance))) {
		    			optionObj = $("<option></option>").attr("value", vmObject.name).prop('selected', true).text(vmName);
		    			primaryVm = vmName;
		    		} else {
						optionObj = $("<option></option>").attr("value", vmObject.name).text(vmName);
		    		}
					$("#vm-select").append(optionObj);
					$(".location-info").html("<span>"+ (userDistance ? "User location: " + (userDistance <= maxDistance ? "Inside " : "Outside ") + "T-Building." : "No user location found.") + "</span><br/><span>Primary application: " + primaryVm + "</span>");
		    	}
		    	// Update material select
		    	$('select').material_select();
		    },
		    beforeSend: function(xhr, settings) { xhr.setRequestHeader('Authorization','Bearer ' + localStorage.bearer_token)},
		   	error: function() {
				$("#db-spinner-container").hide();
				$(".dashboard-error").text("Error getting list of VMs")
			}
		});
	}

	// Haversine formula for calculating distance of two points
	function distanceOfTwoPoints(lon1, lat1, lon2, lat2) {
	  var R = 6371; // Radius of the earth in km
	  var dLat = (lat2-lat1).toRad();  // Javascript functions in radians
	  var dLon = (lon2-lon1).toRad(); 
	  var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	          Math.cos(lat1.toRad()) * Math.cos(lat2.toRad()) * 
	          Math.sin(dLon/2) * Math.sin(dLon/2); 
	  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	  var d = R * c; // Distance in km
	  return Math.floor(d*1000); // Rounded distance in meters
	}

	// Converts numeric degrees to radians
	if (typeof(Number.prototype.toRad) === "undefined") {
	  Number.prototype.toRad = function() {
	    return this * Math.PI / 180;
	  }
	}

});
