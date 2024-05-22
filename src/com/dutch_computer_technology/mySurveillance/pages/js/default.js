window.addEventListener("load", function() {
	let logoutButton = document.getElementById("logoutButton");
	if (logoutButton) {
		logoutButton.addEventListener("click", logout);
	};
	streams();
});

let timeouts = {};
const waitTimeout = 10 * 1000;
async function streams() {
	let canvass = document.getElementsByTagName("canvas");
	if (canvass) {
		for (canvas of canvass) {
			if (!canvas.hasAttribute("src")) continue;
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
		streams();
	}, 10);
};
function loadImage(src) {
	
	if (!src) return false;
	
	if (timeouts.hasOwnProperty(src)) {
		let now = new Date().getTime();
		if (now-timeouts[src] > waitTimeout) {
			delete timeouts[src];
		};
		return false;
	};
	
	return new Promise(function(resolve) {
		let img = new Image;
		img.onload = function() {
			resolve({"img":img,"width":this.width,"height":this.height});
		};
		img.onerror = function() {
			timeouts[src] = new Date().getTime();
			resolve(false);
		};
		img.src = src;
	});
	
};

function clearInputError(elem) {
	
	if (!elem.hasAttribute("id")) return;
	let id = "inputError_" + elem.id;
	let errDiv = document.getElementById(id);
	if (errDiv) errDiv.remove();
	
};
function inputError(elem, err, par) {
	
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

function logout() {
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/accounts");
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