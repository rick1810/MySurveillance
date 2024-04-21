let camNames = [<camNames>];
let cameraTypes = [<cameraTypes>];
let streamTypes = [<streamTypes>];
let cameras = {<camerasJSON>};
window.addEventListener("load", function() {
	loop();
});
async function loop() {
	let canvass = document.getElementsByTagName("canvas");
	if (canvass) {
		for (canvas of canvass) {
			let img = await loadImage(canvas.getAttribute("src"));
			if (!img) continue;
			
			canvas.width = img.width;
			canvas.height = img.height;
			let ctx = canvas.getContext("2d");
			ctx.clearRect(0, 0, img.width, img.height);
			ctx.drawImage(img.img, 0, 0);
		};
	};
	setTimeout(() => {
		loop();
	}, 10);
};
function loadImage(src) {
	
	if (!src) return false;
	
	return new Promise(function(resolve) {
		let img = new Image;
		img.onload = function() {
			resolve({"img":img,"width":this.width,"height":this.height});
		};
		img.onerror = function() {
			resolve(false);
		};
		img.src = src;
	});
	
};

function setScreen(event, screen, name) {
	
	if (name !== false) {
		let xhr = new XMLHttpRequest();
		xhr.open("POST", "/cameras");
		xhr.onreadystatechange = function() {
			if (this.readyState != 4) return;
			if (this.status == 200) {
				location.reload();
			} else {
				let sensor = document.getElementById("sensor");
				if (sensor) sensor.remove();
			};
		};
		let sendData = {"cmd":"setScreen","screen":screen,"name":name};
		xhr.send(JSON.stringify(sendData));
		return;
	};
	
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
	
	let list = "";
	for (cName of camNames) {
		list += "<option class=\\\"option\\\" value=\\\"" + cName + "\\\">" + cName + "</option>";
	};
	
	let div = document.createElement("div");
	div.id = "sensor";
	div.classList = "sensor";
	div.innerHTML = "<select id=\\\"list\\\" class=\\\"list\\\" style=\\\"top: " + event.y + "px; left: " + event.x + "px;\\\">" + list + "</select>";
	div.addEventListener("click", function(event) {
		let elem = event.target;
		if (elem.id == "sensor") {
			let value = "";
			for (elem of elem.children) {
				if (elem.id != "list") continue;
				value = elem.value;
				break;
			};
			elem.remove();
			setScreen(false, screen, value);
		};
	});
	document.body.appendChild(div);
	
};
function screensMin() {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"min"};
	xhr.send(JSON.stringify(sendData));
};
function screensPlus() {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"plus"};
	xhr.send(JSON.stringify(sendData));
};

function closeSettings() {
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
};
function saveSettings(name) {
	
	let sensor = document.getElementById("sensor");
	if (!sensor) return;
	
	let data = cameras.hasOwnProperty(name) ? cameras[name] : {};
	let _name = data.hasOwnProperty("name") ? data["name"] : "";
	let _address = data.hasOwnProperty("address") ? data["address"] : "";
	let _username = data.hasOwnProperty("username") ? data["username"] : "";
	let _password = data.hasOwnProperty("password") ? data["password"] : "";
	let _cameraType = data.hasOwnProperty("cameraType") ? data["cameraType"] : "";
	let _streamType = data.hasOwnProperty("streamType") ? data["streamType"] : "";
	let _streamAddress = data.hasOwnProperty("streamAddress") ? data["streamAddress"] : "";
	
	let elems = sensor.getElementsByTagName("input");
	for (elem of elems) {
		switch(elem.id) {
			case "name":
				_name = elem.value;
				break;
			case "address":
				_address = elem.value;
				break;
			case "username":
				_username = elem.value;
				break;
			case "password":
				_password = elem.value;
				break;
			case "streamAddress":
				_streamAddress = elem.value;
				break;
			default:
				break;
		};
	};
	elems = sensor.getElementsByTagName("select");
	for (elem of elems) {
		switch(elem.id) {
			case "cameraType":
				_cameraType = elem.value;
				break;
			case "streamType":
				_streamType = elem.value;
				break;
			default:
				break;
		};
	};
	
	if (!_name) _name = "";
	if (!name) name = "";
	
	data["oldName"] = name;
	data["name"] = _name;
	data["address"] = _address;
	data["username"] = _username;
	data["password"] = _password;
	data["cameraType"] = _cameraType;
	data["streamType"] = _streamType;
	data["streamAddress"] = _streamAddress;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"save","data":data};
	xhr.send(JSON.stringify(sendData));
	
};
function openSettings(name) {
	
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
	
	let data = cameras.hasOwnProperty(name) ? cameras[name] : {};
	let _name = name ? name : "";
	let _address = data.hasOwnProperty("address") ? data["address"] : "";
	let _username = data.hasOwnProperty("username") ? data["username"] : "";
	let _password = data.hasOwnProperty("password") ? data["password"] : "";
	let _cameraType = data.hasOwnProperty("cameraType") ? data["cameraType"] : "";
	let _streamType = data.hasOwnProperty("streamType") ? data["streamType"] : "";
	let _streamAddress = data.hasOwnProperty("streamAddress") ? data["streamAddress"] : "";
	
	let cameraOptions = "<div><p>CameraType:</p><select id=\\\"cameraType\\\">";
	for (type of cameraTypes) {
		cameraOptions += "<option value=\\\"" + type + "\\\"" + (_cameraType == type ? " selected" : "") + ">" + type + "</option>";
	};
	cameraOptions += "</select></div>";
	
	let streamOptions = "<div><p>StreamType:</p><select id=\\\"streamType\\\">";
	for (type of streamTypes) {
		streamOptions += "<option value=\\\"" + type + "\\\"" + (_streamType == type ? " selected" : "") + ">" + type + "</option>";
	};
	streamOptions += "</select></div>";
	
	let menu = "<div><p>Name:</p><input id=\\\"name\\\" value=\\\"" + _name + "\\\"/></div><div><p>Address:</p><input id=\\\"address\\\" value=\\\"" + _address + "\\\"/></div>"
	 + "<div><p>Username:</p><input id=\\\"username\\\" value=\\\"" + _username + "\\\"/></div><div><p>Password:</p><input id=\\\"password\\\" type=\\\"password\\" value=\\\"" + _password + "\\\"/></div>"
	 + cameraOptions + streamOptions
	 + "<div><p>StreamAddress:</p><input id=\\\"streamAddress\\\" value=\\\"" + _streamAddress + "\\\"/></div>"
	 + "<div class=\\\"buttons\\\"><button onclick=\\\"saveSettings('" + name + "');\\\">Save</button>"
	 + "<button onclick=\\\"removeCamera('" + name + "');\\\">Remove</button>"
	 + "<button onclick=\\\"closeSettings();\\\">Close</button></div>";
	
	let div = document.createElement("div");
	div.id = "sensor";
	div.classList = "sensor";
	div.innerHTML = "<div id=\\\"menu\\\" class=\\\"menu\\\">" + menu + "</div>";
	document.body.appendChild(div);
	
};
function removeCamera(name) {
	
	if (!name) return;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"remove","name":name};
	xhr.send(JSON.stringify(sendData));
	
};
function power(name) {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"power","name":name};
	xhr.send(JSON.stringify(sendData));
};
function glitch(name) {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"glitch","name":name};
	xhr.send(JSON.stringify(sendData));
};