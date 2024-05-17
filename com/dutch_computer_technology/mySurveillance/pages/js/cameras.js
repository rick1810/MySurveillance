let camNames = [<camNames>];
let cameraTypes = [<cameraTypes>];
let streamTypes = [<streamTypes>];
let pathTypes = [<pathTypes>];
let reasonTypes = [<reasonTypes>];
let cameras = {<camerasJSON>};

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
	
	let data = {};
	
	let elems = sensor.getElementsByTagName("input");
	for (elem of elems) {
		switch(elem.id) {
			case "name":
				data["name"] = elem.value;
				break;
			case "address":
				data["address"] = elem.value;
				break;
			case "username":
				data["username"] = elem.value;
				break;
			case "password":
				data["password"] = elem.value;
				break;
			case "streamAddress":
				data["streamAddress"] = elem.value;
				break;
			default:
				break;
		};
	};
	elems = sensor.getElementsByTagName("select");
	for (elem of elems) {
		switch(elem.id) {
			case "cameraType":
				data["cameraType"] = elem.value;
				break;
			case "streamType":
				data["streamType"] = elem.value;
				break;
			case "pathType":
				data["path"] = {"type":elem.value};
			case "reasonType":
				data["reason"] = {"type":elem.value};
			default:
				break;
		};
	};
	let path = document.getElementById("path");
	if (path) {
		let pathInputs = path.getElementsByTagName("input");
		for (pathInput of pathInputs) {
			if (!pathInput.hasAttribute("key")) continue;
			data["path"][pathInput.getAttribute("key")] = pathInput.value;
		};
		let pathSelects = path.getElementsByTagName("input");
		for (pathSelect of pathSelects) {
			if (!pathSelect.hasAttribute("key")) continue;
			data["path"][pathSelect.getAttribute("key")] = pathSelect.value;
		};
	};
	let reason = document.getElementById("reason");
	if (reason) {
		let reasonInputs = reason.getElementsByTagName("input");
		for (reasonInput of reasonInputs) {
			if (!reasonInput.hasAttribute("key")) continue;
			data["reason"][reasonInput.getAttribute("key")] = reasonInput.value;
		};
		let reasonSelects = reason.getElementsByTagName("input");
		for (reasonSelect of reasonSelects) {
			if (!reasonSelect.hasAttribute("key")) continue;
			data["reason"][reasonSelect.getAttribute("key")] = reasonSelect.value;
		};
	};
	
	if (name) data["oldName"] = name;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"save","data":data};
	xhr.send(JSON.stringify(sendData));
	
};
async function getPath(data, name) {
	let type = "";
	if (data.hasOwnProperty("path")) {
		if (data["path"].hasOwnProperty("type")) {
			type = data["path"]["type"];
		};
	};
	
	let html = "<div class=\\\"path\\\"><div class=\\\"options\\\"><p>PathType:</p><select id=\\\"pathType\\\">";
	for (myType of pathTypes) {
		html += "<option value=\\\"" + myType + "\\\"" + (type == myType ? " selected" : "") + ">" + myType + "</option>";
	};
	html += "</select></div>";
	html += "<div id=\\\"path\\\" class=\\\"inner\\\">" + await loadPath(type, name) + "</div></div>";
	return html;
};
function loadPath(type, name) {
	return new Promise(function (resolve) {
		let xhr = new XMLHttpRequest();
		xhr.open("POST", "/cameras");
		xhr.onreadystatechange = function() {
			if (this.readyState != 4) return;
			resolve(this.response);
		};
		let sendData = {"cmd":"getPath","type":type,"name":name};
		xhr.send(JSON.stringify(sendData));
	});
};
async function getReason(data, name) {
	let type = "";
	if (data.hasOwnProperty("reason")) {
		if (data["reason"].hasOwnProperty("type")) {
			type = data["reason"]["type"];
		};
	};
	
	let html = "<div class=\\\"reason\\\"><div class=\\\"options\\\"><p>ReasonType:</p><select id=\\\"reasonType\\\">";
	for (myType of reasonTypes) {
		html += "<option value=\\\"" + myType + "\\\"" + (type == myType ? " selected" : "") + ">" + myType + "</option>";
	};
	html += "</select></div>";
	html += "<div id=\\\"reason\\\" class=\\\"inner\\\">" + await loadReason(type, name) + "</div></div>";
	return html;
};
function loadReason(type, name) {
	return new Promise(function (resolve) {
		let xhr = new XMLHttpRequest();
		xhr.open("POST", "/cameras");
		xhr.onreadystatechange = function() {
			if (this.readyState != 4) return;
			resolve(this.response);
		};
		let sendData = {"cmd":"getReason","type":type,"name":name};
		xhr.send(JSON.stringify(sendData));
	});
};
async function openSettings(name) {
	
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
	 + await getPath(data, name)
	 + await getReason(data, name)
	 + "<div class=\\\"buttons\\\"><button onclick=\\\"saveSettings('" + name + "');\\\">Save</button>"
	 + "<button onclick=\\\"removeCamera('" + name + "');\\\">Remove</button>"
	 + "<button onclick=\\\"closeSettings();\\\">Close</button></div>";
	
	let div = document.createElement("div");
	div.id = "sensor";
	div.classList = "sensor";
	div.innerHTML = "<div id=\\\"menu\\\" class=\\\"menu\\\">" + menu + "</div>";
	document.body.appendChild(div);
	
	let pathType = document.getElementById("pathType");
	if (pathType) pathType.addEventListener("change", async function(event) {
		let path = document.getElementById("path");
		if (path) path.innerHTML = await loadPath(pathType.value, name);
	});
	
	let reasonType = document.getElementById("reasonType");
	if (reasonType) reasonType.addEventListener("change", async function(event) {
		let reason = document.getElementById("reason");
		if (reason) reason.innerHTML = await loadReason(reasonType.value, name);
	});
	
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