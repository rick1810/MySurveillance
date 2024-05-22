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
	
	let nameElem = document.getElementById("name");
	if (name) data["oldName"] = name;
	if (nameElem) {
		data["name"] = nameElem.value;
		console.log(data["name"]);
		if (data["name"].length == 0) {
			inputError(nameElem, "No name");
			return;
		};
	};
	clearInputError(nameElem);
	
	let cameraTypeElem = document.getElementById("cameraType");
	if (cameraTypeElem) data["cameraType"] = cameraTypeElem.value;
	
	let streamTypeElem = document.getElementById("streamType");
	if (streamTypeElem) data["stream"] = {"type":streamTypeElem.value};
	
	let pathTypeElem = document.getElementById("pathType");
	if (pathTypeElem) data["path"] = {"type":pathTypeElem.value};
	
	let reasonTypeElem = document.getElementById("reasonType");
	if (reasonTypeElem) data["reason"] = {"type":reasonTypeElem.value};
	
	if (data.hasOwnProperty("stream")) {
		let stream = document.getElementById("stream");
		if (stream) {
			let streamInputs = stream.getElementsByTagName("input");
			for (streamInput of streamInputs) {
				if (!streamInput.hasAttribute("key")) continue;
				let value = streamInput.value;
				if (streamInput.type == "checkbox") value = streamInput.checked;
				data["stream"][streamInput.getAttribute("key")] = value;
			};
			let streamSelects = stream.getElementsByTagName("select");
			for (streamSelect of streamSelects) {
				if (!streamSelect.hasAttribute("key")) continue;
				data["stream"][streamSelect.getAttribute("key")] = streamSelect.value;
			};
		};
	};
	
	if (data.hasOwnProperty("path")) {
		let path = document.getElementById("path");
		if (path) {
			let pathInputs = path.getElementsByTagName("input");
			for (pathInput of pathInputs) {
				if (!pathInput.hasAttribute("key")) continue;
				let value = pathInput.value;
				if (pathInput.type == "checkbox") value = pathInput.checked;
				data["path"][pathInput.getAttribute("key")] = value;
			};
			let pathSelects = path.getElementsByTagName("select");
			for (pathSelect of pathSelects) {
				if (!pathSelect.hasAttribute("key")) continue;
				data["path"][pathSelect.getAttribute("key")] = pathSelect.value;
			};
		};
	};
	
	if (data.hasOwnProperty("reason")) {
		let reason = document.getElementById("reason");
		if (reason) {
			let reasonInputs = reason.getElementsByTagName("input");
			for (reasonInput of reasonInputs) {
				if (!reasonInput.hasAttribute("key")) continue;
				let value = reasonInput.value;
				if (reasonInput.type == "checkbox") value = reasonInput.checked;
				data["reason"][reasonInput.getAttribute("key")] = value;
			};
			let reasonSelects = reason.getElementsByTagName("select");
			for (reasonSelect of reasonSelects) {
				if (!reasonSelect.hasAttribute("key")) continue;
				data["reason"][reasonSelect.getAttribute("key")] = reasonSelect.value;
			};
		};
	};
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"save","data":data};
	xhr.send(JSON.stringify(sendData));
	
};
async function getStream(data, name) {
	let type = "";
	if (data.hasOwnProperty("stream")) {
		if (data["stream"].hasOwnProperty("type")) {
			type = data["stream"]["type"];
		};
	};
	
	let html = "<div class=\\\"stream\\\"><div class=\\\"options\\\"><p>StreamType:</p><select id=\\\"streamType\\\">";
	for (myType of streamTypes) {
		html += "<option value=\\\"" + myType + "\\\"" + (type == myType ? " selected" : "") + ">" + myType + "</option>";
	};
	html += "</select></div>";
	html += "<div id=\\\"stream\\\" class=\\\"inner\\\">" + await loadStream(type, name) + "</div></div>";
	return html;
};
function loadStream(type, name) {
	return new Promise(function (resolve) {
		let xhr = new XMLHttpRequest();
		xhr.open("POST", "/cameras");
		xhr.onreadystatechange = function() {
			if (this.readyState != 4) return;
			resolve(this.response);
		};
		let sendData = {"cmd":"getStream","type":type,"name":name};
		xhr.send(JSON.stringify(sendData));
	});
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
	let _cameraType = data.hasOwnProperty("cameraType") ? data["cameraType"] : "";
	
	let cameraOptions = "<div><p>CameraType:</p><select id=\\\"cameraType\\\">";
	for (type of cameraTypes) {
		cameraOptions += "<option value=\\\"" + type + "\\\"" + (_cameraType == type ? " selected" : "") + ">" + type + "</option>";
	};
	cameraOptions += "</select></div>";
	
	let rem = "";
	if (name) rem = "<button onclick=\\\"removeCamera('" + name + "');\\\">{{input.remove}}</button>";
	let save = "<button onclick=\\\"saveSettings();\\\">{{input.save}}</button>";
	if (name) save = "<button onclick=\\\"saveSettings('" + name + "');\\\">{{input.save}}</button>";
	
	let menu = "<div><p>Name:</p><input id=\\\"name\\\" value=\\\"" + _name + "\\\"/></div>"
	 + cameraOptions
	 + await getStream(data, name)
	 + await getPath(data, name)
	 + await getReason(data, name)
	 + "<div class=\\\"buttons\\\" id=\\\"buttons\\\">" + rem
	 + save
	 + "<button onclick=\\\"closeSettings();\\\">{{input.close}}</button></div>";
	
	let div = document.createElement("div");
	div.id = "sensor";
	div.classList = "sensor";
	div.innerHTML = "<div id=\\\"menu\\\" class=\\\"menu\\\">" + menu + "</div>";
	document.body.appendChild(div);
	
	let streamType = document.getElementById("streamType");
	if (streamType) streamType.addEventListener("change", async function(event) {
		let stream = document.getElementById("stream");
		if (stream) stream.innerHTML = await loadStream(streamType.value, name);
	});
	
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
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"remove","name":name};
	xhr.send(JSON.stringify(sendData));
	
};
function power(name) {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"power","name":name};
	xhr.send(JSON.stringify(sendData));
};
function glitch(name) {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/cameras");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"glitch","name":name};
	xhr.send(JSON.stringify(sendData));
};