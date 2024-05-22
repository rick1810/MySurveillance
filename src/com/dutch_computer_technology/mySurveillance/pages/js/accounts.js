let users = {<usersJSON>};

function openUser(username) {
	
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
	
	let data = users.hasOwnProperty(username) ? users[username] : {};
	let _username = username ? username : "";
	let _isAdmin = data.hasOwnProperty("isAdmin") ? data["isAdmin"] : false;
	
	let rem = "";
	if (username) rem = "<button onclick=\\\"removeUser('" + username + "');\\\">{{input.remove}}</button>";
	let save = "<button onclick=\\\"saveUser();\\\">{{input.save}}</button>";
	if (username) save = "<button onclick=\\\"saveUser('" + username + "');\\\">{{input.save}}</button>";
	
	let menu = "<div><div class=\\\"icon\\\">" + (_isAdmin ? "<img src=\\\"admin.png\\\"/>" : "<img src=\\\"user.png\\\"/>")
	+ "</div><div class=\\\"wrapper\\\"><div class=\\\"inputs\\\"><div><p>{{accounts.username}}:</p><input id=\\\"username\\\" value=\\\"" + _username + "\\\"/></div>"
	+ "<div><p>{{accounts.password}}:</p><input id=\\\"password\\\" maxlength=28 type=\\\"password\\\"/></div>"
	+ "<div><p>{{accounts.isAdmin}}:</p><input id=\\\"isAdmin\\\"" + (_isAdmin ? " checked" : "") + " type=\\\"checkbox\\\"/></div></div>"
	+ "<div class=\\\"buttons\\\" id=\\\"buttons\\\">" + rem + save + "<button onclick=\\\"closeUser();\\\">{{input.close}}</button></div></div></div>";
	
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
	
	let sendData = {"cmd":"save"};
	
	let usernameElem = document.getElementById("username");
	if (usernameElem) _username = usernameElem.value;
	
	if (_username == "") {
		inputError(usernameElem, "No username");
		return;
	};
	sendData["username"] = (username ? username : _username);
	if (_username != username) {
		if (users.hasOwnProperty(_username)) {
			inputError(usernameElem, "Username already taken");
			return;
		};
		sendData["newUsername"] = _username;
	};
	clearInputError(usernameElem);
	
	let passwordElem = document.getElementById("password");
	if (passwordElem) _password = passwordElem.value;
	
	if (_password) {
		if (_password.length > 0) {
			if (_password.length < 4) {
				inputError(passwordElem, "Password must be atleast 4 characters long");
				return;
			};
			sendData["password"] = _password;
		};
	} else if (!username) {
		inputError(passwordElem, "Password needed for new User");
		return;
	};
	clearInputError(passwordElem);
	
	let isAdminElem = document.getElementById("isAdmin");
	if (isAdminElem) _isAdmin = isAdminElem.checked;
	sendData["isAdmin"] = _isAdmin;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/accounts");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		
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
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"remove","username":username};
	xhr.send(JSON.stringify(sendData));
	
};