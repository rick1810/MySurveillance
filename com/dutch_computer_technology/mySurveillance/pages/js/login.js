window.addEventListener("load", function() {
	let loginButton = document.getElementById("loginButton");
	if (loginButton) {
		loginButton.addEventListener("click", login);
	};
});
function login() {
	let usernameInput = document.getElementById("usernameInput");
	if (!usernameInput) return;
	let passwordInput = document.getElementById("passwordInput");
	if (!passwordInput) return;
	
	let username = usernameInput.value;
	if (username.length < 3) return;
	if (username.length > 128) return;
	
	let password = passwordInput.value;
	if (password.length < 4) return;
	if (password.length > 128) return;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/login");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) return;
		
		let date = new Date();
		date.setDate(date.getDate() + 31);
		document.cookie = "token=" + this.response.toString() + ";expires=" + date.toGMTString() + ";Path=/;";
		location.replace("/");
	};
	let sendData = {"username": username, "password": password};
	xhr.send(JSON.stringify(sendData));
};