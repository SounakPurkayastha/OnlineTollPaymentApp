var express = require("express"),
    app = express(),
    mongoose = require("mongoose"),
    admin = require("firebase-admin"),
    serviceAccount = require("G:/OnlineTollPaymentApp/onlinetollpaymentapp-bf63a-firebase-adminsdk-2pxp2-b8762aab57.json"),
    bodyParser = require("body-parser");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://onlinetollpaymentapp-bf63a.firebaseio.com"
})

app.set("view engine", "ejs");
app.use(bodyParser.urlencoded({extended : true}));
app.use(express.static(__dirname + "/public"));
mongoose.connect("mongodb://localhost/qrcodes",{useNewUrlParser:true, useUnifiedTopology:true});

var QRSchema = new mongoose.Schema({
    vehicleId: String,
    qrcode: String
});

var QRCode = mongoose.model("QRCode", QRSchema);

app.get("/", function(req, res) {
    res.send("Home Page");
});

app.get("/:id", function(req, res) {
    if(RegExp("[A-Z]{2}[0-9]{6}").test(req.params.id)) {
        var code = makeCode();
        var newQR = {vehicleId: req.params.id, qrcode: code};
        QRCode.create(newQR);
        res.send({qrcode:code});
    }
});

app.get("/remove/:qrcode", function(req, res) {
    QRCode.findOneAndDelete({qrcode: req.params.qrcode}, function(err) {
        if(!err)
            res.redirect("/");
    });
});

app.get("/find/:id", function(req, res) {
    QRCode.find({vehicleId: req.params.id}, function(err,found) {
        if(!err){
            res.send({qrcode: found[0].qrcode});
        }
        else{
            res.send(err);
        }
    });
});

app.get("/response/:token", function(req, res) {
    var payload = {
        notification: {
            title:"Notification",
            body:"Asshole"
        }
    }
    admin.messaging().sendToDevice(req.params.token,payload).then(function(response){
        console.log('Successfully sent message',response);
        res.redirect('/');
    }).catch(function(error) {
        res.send(error);
    })
})

function makeCode() {
    var result           = '';
    var characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for ( var i = 0; i < 10; i++ ) {
       result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

app.listen(8000, function() {
    console.log("Serving app on port 8000");
});