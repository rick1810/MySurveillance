window.addEventListener("load", function() {
	let logoutButton = document.getElementById("logoutButton");
	if (logoutButton) {
		logoutButton.addEventListener("click", logout);
	};
});
function logout() {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/account");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		let date = new Date(0);
		document.cookie = "token=;expires=" + date.toGMTString() + ";Path=/;";
		location.reload();
	};
	let sendData = {"cmd":"logout"};
	xhr.send(JSON.stringify(sendData));
};
function goTo(event, page) {
	if (event.button == 1) {
		window.open("/" + page);
	} else {
		location.href = "/" + page;
	};
};