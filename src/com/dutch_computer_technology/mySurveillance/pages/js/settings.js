window.addEventListener("load", function() {
	let path = document.getElementById("path");
	if (path) path.addEventListener("change", function(event) { savePath(event); });
});

function savePath(event) {
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/settings");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status == 200) location.reload();
	};
	let sendData = {"cmd":"setPath","path":event.target.value,"name":name};
	xhr.send(JSON.stringify(sendData));
	return;
	
};