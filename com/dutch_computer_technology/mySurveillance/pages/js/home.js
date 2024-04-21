window.addEventListener("load", function() {
	loop();
});
async function loop() {
	let canvass = document.getElementsByTagName("canvas");
	if (canvass) {
		for (canvas of canvass) {
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
		loop();
	}, 10);
};

function loadImage(src) {
	
	if (!src) return false;
	
	return new Promise(function(resolve) {
		let img = new Image;
		img.onload = function() {
			resolve({"img":img,"width":this.width,"height":this.height});
		};
		img.onerror = function() {
			resolve(false);
		};
		img.src = src;
	});
	
};