'use strict'

const userModel = require('../../db/models/user')
const logger = require('../../logger')
const jwt = require('jsonwebtoken')
const validator = require('validator')
const gcompute = require('./helpers/gcompute')

const lengthValidatorOptions = {min: 5, max: 100}

function getDetails(req, res, next) {
    if (req.user) {
        return res.send(req.user)
    }
    return next(new Error('Invalid user!'))
}

function getVMS(req, res, next) {
    gcompute.getVMS((err, vmJSON) => {
        if (err) { console.log(err); console.log(err.stack); return next(err) }
        try {
            var vms = []
            const array = vmJSON.items['zones/europe-west1-d']['instances']
            array.forEach((dict) => {
                if (dict.metadata.items) {
                    var vm = {
                        'name': dict.name,
                        'desc': dict.description,
                        'sts': dict.status,
                        'address': dict.networkInterfaces[0].accessConfigs[0].natIP
                    }
                    dict.metadata.items.forEach(metaDict => {
                        vm[metaDict.key] = metaDict.value
                    })
                    vms.push(vm)
                }
            })
            res.send(vms)
        } catch (err) {
            console.log("VMS parsing error: " + err)
            //return next(err)
            res.status(500).send(vmJSON.items['zones/europe-west1-d']['instances'])
        }
    })
}

function registerUser(req, res, next) {
    const email = req.body.email
    const password = req.body.password
    const name = req.body.name

    if (validator.isEmail(email) &&
        validator.isLength(password, lengthValidatorOptions) &&
        validator.isLength(name, lengthValidatorOptions)) 
        {
            userModel.create({email, password, name}, (err, user) => {
                if (err) {
                    return next(err)
                } else if (!user) {
                    return next(new Error('Could not create user!'))
                }
                res.status(201).send({ message: 'Success!' }) 
        })
    } else {
        next(new Error(`User sign up invalid payload!`))
    }
}

function authenticateUser(req, res, next) {
    
    const email = req.body.email
    const password = req.body.password

    if (validator.isEmail(email) && 
        validator.isLength(password, lengthValidatorOptions)) {
            // payload valid
            userModel.findOne({email}, (err, user) => {
                if (err) {
                    return next(err)
                } else if (!user) {
                    return res.status(403).send({ message: 'Authentication failed.' })
                }

                user.comparePassword(password).then((isValid) => {
                    if (isValid == true) {
                        const token = jwt.sign(user.toJSON(), process.env.SECRET)
                        res.send({token})
                    } else {
                        return res.status(403).send({ message: 'Authentication failed.' })
                    }
                }).catch((err) => {
                    next(err)
                })
            })
        } else {
            next(new Error(`User login invalid payload!`))
        }
}

function stopVM(req, res, next) {
    gcompute.powerOffVM(req.body.instance, true, (err, result) => {
        if (err) { return next(err) }
        res.send(result)
    })
}

function startVM(req, res, next) {
    gcompute.powerOffVM(req.body.instance, false, (err, result) => {
        if (err) { return next(err) }
        res.send(result)
    })
}

const routes = { GET: {'/details' : getDetails, '/vms' : getVMS},
                 POST: {'/auth' : authenticateUser, '/vms/stop' : stopVM, '/vms/start' : startVM},
                 PUT: {'/register' : registerUser}
               }


module.exports = {routes}