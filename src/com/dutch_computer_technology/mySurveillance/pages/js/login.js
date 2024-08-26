var ready = true;
window.addEventListener("load", function() {
	let loginButton = document.getElementById("loginButton");
	if (loginButton) {
		loginButton.addEventListener("click", login);
	};
	let usernameInput = document.getElementById("usernameInput");
	if (usernameInput) {
		usernameInput.addEventListener("keydown", function(e) {
			if (e.keyCode == 13 || e.keyCode == 40) {
				let passwordInput = document.getElementById("passwordInput");
				if (passwordInput) passwordInput.focus();
			};
		});
	};
	let passwordInput = document.getElementById("passwordInput");
	if (passwordInput) {
		passwordInput.addEventListener("keydown", function(e) {
			if (e.keyCode == 38) {
				let usernameInput = document.getElementById("usernameInput");
				if (usernameInput) usernameInput.focus();
			} else if (e.keyCode == 13) {
				let loginButton = document.getElementById("loginButton");
				if (loginButton) loginButton.click();
			};
		});
	};
});
function login() {
	
	if (!ready) return;
	ready = false;
	
	let usernameInput = document.getElementById("usernameInput");
	if (!usernameInput) return;
	let passwordInput = document.getElementById("passwordInput");
	if (!passwordInput) return;
	let loginButton = document.getElementById("loginButton");
	if (!loginButton) return;
	
	let username = usernameInput.value;
	if (username.length < 3) {
		inputError(loginButton, "{{credentials.incorrect}}", true);
		return;
	};
	if (username.length > 128) {
		inputError(loginButton, "{{credentials.incorrect}}", true);
		return;
	};
	
	let password = passwordInput.value;
	if (password.length < 4) {
		inputError(loginButton, "{{credentials.incorrect}}", true);
		return;
	};
	if (password.length > 128) {
		inputError(loginButton, "{{credentials.incorrect}}", true);
		return;
	};
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/login");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			inputError(loginButton, this.response, true);
			return;
		};
		
		let date = new Date();
		date.setDate(date.getDate() + 31);
		document.cookie = "token=" + this.response.toString() + ";expires=" + date.toGMTString() + ";Path=/;";
		location.replace("/");
	};
	let sendData = {"username": username, "password": password};
	xhr.send(JSON.stringify(sendData));
};
function loginWait() {
	
	if (ready) return;
	setTimeout(() => {
		ready = true;
	}, 5000); //5 Sec
	
};
function inputError(elem, err, par) {
	
	loginWait();
	
	if (!elem) return;
	if (!elem.hasAttribute("id")) return;
	if (!err) return;
	
	let id = "inputError_" + elem.id;
	
	let errDiv = document.getElementById(id);
	if (errDiv) errDiv.remove();
	
	errDiv = document.createElement("div");
	errDiv.id = id;
	errDiv.classList = "inputError";
	errDiv.innerHTML = "<p>" + err + "</p>";
	
	let wrapper = elem;
	if (!par) wrapper = elem.parentElement;
	wrapper.parentElement.insertBefore(errDiv, wrapper);
	
};