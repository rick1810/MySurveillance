let users = {<usersJSON>};

function openUser(username) {
	
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
	
	let data = users.hasOwnProperty(username) ? users[username] : {};
	let _username = username ? username : "";
	let _isAdmin = data.hasOwnProperty("isAdmin") ? data["isAdmin"] : false;
	
	let rem = "";
	if (username) rem = "<button onclick=\\\"removeUser('" + username + "');\\\">Remove</button>";
	let save = "<button onclick=\\\"saveUser();\\\">Save</button>";
	if (username) save = "<button onclick=\\\"saveUser('" + username + "');\\\">Save</button>";
	
	let menu = "<div><div class=\\\"icon\\\">" + (_isAdmin ? "<img src=\\\"admin.png\\\"/>" : "<img src=\\\"user.png\\\"/>")
	+ "</div><div class=\\\"wrapper\\\"><div class=\\\"inputs\\\"><div><p>Username:</p><input id=\\\"username\\\" value=\\\"" + _username + "\\\"/></div>"
	+ "<div><p>Password:</p><input id=\\\"password\\\" maxlength=28 type=\\\"password\\\"/></div>"
	+ "<div><p>isAdmin:</p><input id=\\\"isAdmin\\\"" + (_isAdmin ? " checked" : "") + " type=\\\"checkbox\\\"/></div></div>"
	+ "<div class=\\\"buttons\\\">" + rem + save + "<button onclick=\\\"closeUser();\\\">Close</button></div></div></div>";
	
	let div = document.createElement("div");
	div.id = "sensor";
	div.classList = "sensor";
	div.innerHTML = "<div id=\\\"menu\\\" class=\\\"menu\\\">" + menu + "</div>";
	document.body.appendChild(div);
	
};

function saveUser(username) {
	
	let sensor = document.getElementById("sensor");
	if (!sensor) return;
	
	let data = users.hasOwnProperty(username) ? users[username] : {};
	let _username = username ? username : "";
	let _password = false;
	let _isAdmin = data.hasOwnProperty("isAdmin") ? data["isAdmin"] : false;
	
	let elems = sensor.getElementsByTagName("input");
	for (elem of elems) {
		switch(elem.id) {
			case "username":
				_username = elem.value;
				break;
			case "password":
				_password = elem.value;
				break;
			case "isAdmin":
				_isAdmin = elem.checked;
				break;
			default:
				break;
		};
	};
	
	if (_username == "") return;
	
	let sendData = {"cmd":"save"};
	
	sendData["username"] = (username ? username : _username);
	if (_username != username) {
		if (users.hasOwnProperty(_username)) return;
		sendData["newUsername"] = _username;
	};
	
	if (_password) {
		if (_password.length < 4) return;
		sendData["password"] = _password;
	} else if (!username) return;
	
	sendData["isAdmin"] = _isAdmin;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/accounts");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) return;
		
		let token = this.response.toString();
		if (token.length > 0) {
			let date = new Date();
			date.setDate(date.getDate() + 31);
			document.cookie = "token=" + token + ";expires=" + date.toGMTString() + ";Path=/;";
		};
		
		location.reload();
	};
	xhr.send(JSON.stringify(sendData));
	
};

function closeUser() {
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
};

function removeUser(username) {
	
	if (!username) return;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/accounts");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"remove","username":username};
	xhr.send(JSON.stringify(sendData));
	
};