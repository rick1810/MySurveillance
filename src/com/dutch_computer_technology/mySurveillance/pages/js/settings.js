let languages = [<languages>];
let colors = {
	"primary":"<primaryColor>",
	"secondary":"<secondaryColor>",
	"tertiary":"<tertiaryColor>",
	"input":"<inputColor>",
	"inputBorder":"<inputBorderColor>",
	"banner":"<bannerColor>"
};

function openLangauge() {
	
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
	
	let langs = "";
	for (language of languages) {
		langs += "<button onclick=\\\"saveLanguage('" + language + "');\\\"><img src=\\\"" + language + ".png\\\"></button>";
	};
	
	let menu = "<div class=\\\"languages\\\">" + langs + "</div>"
	+ "<div class=\\\"buttons\\\" id=\\\"buttons\\\"><button onclick=\\\"resetLanguage();\\\">{{input.reset}}</button><button onclick=\\\"closeLanguage();\\\">{{input.close}}</button></div>";
	
	let div = document.createElement("div");
	div.id = "sensor";
	div.classList = "sensor";
	div.innerHTML = "<div id=\\\"menu\\\" class=\\\"menu\\\">" + menu + "</div>";
	document.body.appendChild(div);
	
};
function saveLanguage(langName) {
	
	let sensor = document.getElementById("sensor");
	if (!sensor) return;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/settings");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"saveLanguage","name":langName};
	xhr.send(JSON.stringify(sendData));
	
};
function resetLanguage() {
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/settings");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"resetLanguage"};
	xhr.send(JSON.stringify(sendData));
	
};
function closeLanguage() {
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
};

function openColor(colorName) {
	
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
	
	let color = colors.hasOwnProperty(colorName) ? colors[colorName] : "<primaryColor>";
	
	let menu = "<div><div class=\\\"colorBlock\\\" id=\\\"colorBlock\\\" style=\\\"background-color: " + color + ";\\\"></div></div>"
	+ "<div><p>Hex:</p><input id=\\\"colorInput\\\" value=\\\"" + color + "\\\"/></div>"
	+ "<div class=\\\"buttons\\\" id=\\\"buttons\\\"><button onclick=\\\"resetColor('" + colorName + "');\\\">{{input.reset}}</button><button onclick=\\\"saveColor('" + colorName + "');\\\">{{input.save}}</button><button onclick=\\\"closeColor();\\\">{{input.close}}</button></div>";
	
	let div = document.createElement("div");
	div.id = "sensor";
	div.classList = "sensor";
	div.innerHTML = "<div id=\\\"menu\\\" class=\\\"menu\\\">" + menu + "</div>";
	document.body.appendChild(div);
	
	let colorInput = document.getElementById("colorInput");
	if (colorInput) colorInput.addEventListener("change", function(event) {
		
		let hex = colorInput.value;
		if (!hex.startsWith("#")) hex = "#" + hex;
		let regex = /^#([0-9A-Fa-f]{3}){1,2}\$/;
		
		if (!regex.test(hex)) {
			inputError(colorInput, "Invalid hex");
			return;
		};
		clearInputError(colorInput);
		
		colorInput.value = hex;
		let colorBlock = document.getElementById("colorBlock");
		colorBlock.style = "background-color: " + hex;
	});
	
};
function saveColor(colorName) {
	
	let sensor = document.getElementById("sensor");
	if (!sensor) return;
	
	if (!colors.hasOwnProperty(colorName)) return;
	
	let colorInput = document.getElementById("colorInput");
	if (!colorInput) return;
	
	let hex = colorInput.value;
	if (!hex.startsWith("#")) hex = "#" + hex;
	let regex = /^#([0-9A-Fa-f]{3}){1,2}\$/;
	if (!regex.test(hex)) return;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/settings");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"setColor","name":colorName,"hex":hex};
	xhr.send(JSON.stringify(sendData));
	
};
function resetColor(colorName) {
	
	if (!colors.hasOwnProperty(colorName)) return;
	
	let xhr = new XMLHttpRequest();
	xhr.open("POST", "/settings");
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if (this.status != 200) {
			let buttons = document.getElementById("buttons");
			inputError(buttons, this.response, true);
			return;
		};
		location.reload();
	};
	let sendData = {"cmd":"resetColor","name":colorName};
	xhr.send(JSON.stringify(sendData));
	
};
function closeColor() {
	let sensor = document.getElementById("sensor");
	if (sensor) sensor.remove();
};