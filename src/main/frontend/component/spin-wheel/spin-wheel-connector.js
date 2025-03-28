import {Wheel} from "spin-wheel";

window.wheel = async (element, serverConf) => {

    let modifier = 0;

    const images = [];

    const conf = {
        radius: 0.84,
        itemLabelRadius: 0.93,
        itemLabelRadiusMax: 0.35,
        itemLabelRotation: 180,
        itemLabelAlign: 'left',
        itemLabelColors: ['#fff'],
        itemLabelBaselineOffset: -0.07,
        itemLabelFont: 'Amatic SC',
        itemLabelFontSizeMax: 55,
        itemBackgroundColors: ['#ffc93c', '#66bfbf', '#a2d5f2', '#515070', '#43658b', '#ed6663', '#d54062' ],
        rotationSpeedMax: 500,
        rotationResistance: -100,
        lineWidth: 1,
        lineColor: '#fff',
        image: './img/example-0-image.svg',
        overlayImage: './img/example-0-overlay.svg'
    }

    // override defaults with provided values
    Object.assign(conf, serverConf);

    // Convert image urls into actual images:
    images.push(initImage(conf, 'image'));
    images.push(initImage(conf, 'overlayImage'));

    await loadImages(images);

    conf.onRest = e => {
        const event = new Event("wheel-rest");
        event.detail = {
            index : e.currentIndex,
            rotation : e.rotation
        }
        element.dispatchEvent(event);
    };

    const wheel = new Wheel(element, conf);

    //create a function a global function to spin the wheel
    window.randomSpin = () => {
        let {winningItemRotation, duration} = calcSpinToValues();
        wheel.spinTo(winningItemRotation, duration, null);
    }

    function calcSpinToValues() {
        const duration = 3000;
        const winningItemRotation = getRandomInt(360, 360 * 1.75) + modifier;
        modifier += 360 * 1.75;
        return {duration, winningItemRotation};
    }

    function getRandomInt(min, max) {
        min = Math.ceil(min);
        max = Math.floor(max);
        return Math.floor(Math.random() * (max - min)) + min;
    }

    function initImage(obj, pName) {
        if (!obj[pName]) return null;
        const i = new Image();
        i.src = obj[pName];
        obj[pName] = i;
        return i;
    }

    async function loadImages(images = []) {
        const promises = [];

        for (const img of images) {
            if (img instanceof HTMLImageElement) promises.push(img.decode());
        }

        try {
            await Promise.all(promises);
        } catch (error) {
            throw new Error('An image could not be loaded');
        }
    }
}

