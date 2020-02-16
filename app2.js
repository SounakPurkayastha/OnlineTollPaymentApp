var express = require("express"),
    app = express(),
    mongoose = require("mongoose"),
    admin = require("firebase-admin"),
    serviceAccount = require("/home/ec2-user/onlinetollpaymentapp-bf63a-firebase-adminsdk-2pxp2-b8762aab57.json"),
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
    token: String,
    qrcode: String
});

var QRCode = mongoose.model("QRCode", QRSchema);

app.get("/", function(req, res) {
    res.send("Home Page");
});

app.get("/:token", function(req, res) {
    //if(RegExp("[A-Z]{2}[0-9]{6}").test(req.params.token)) {
        var code = makeCode();
        var newQR = {token: req.params.token, qrcode: code};
        QRCode.create(newQR);
        res.send({qrcode:code});
    //}
});

app.get("/remove/:qrcode", function(req, res) {
    QRCode.findOneAndDelete({qrcode: req.params.qrcode}, function(err) {
        if(!err)
            res.redirect("/");
    });
});

app.get("/find/:qrcode", function(req, res) {
    QRCode.find({qrcode: req.params.qrcode}, function(err,found) {
        if(!err){
            res.send({token: found[0].token});
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
            body:"Notification"
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