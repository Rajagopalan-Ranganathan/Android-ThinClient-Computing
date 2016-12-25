#!/bin/bash

# USER=${MONGODB_USER:-"admin"}
# DATABASE=${MONGODB_DATABASE:-"admin"}
# PASS=${MONGODB_PASS:-$(pwgen -s 12 1)}
USER=${DB_USER}
DATABASE=${DB_NAME}
TEST_DATABASE=${DB_TEST_NAME}
PASS=${DB_PASS} 

_word=$( [ ${MONGODB_PASS} ] && echo "preset" || echo "random" )

RET=1
while [[ RET -ne 0 ]]; do
    echo "=> Waiting for confirmation of MongoDB service startup"
    sleep 5
    mongo admin --eval "help" >/dev/null 2>&1
    RET=$?
done

echo "=> Creating an ${USER} user with a ${_word} password in MongoDB - ${TEST_DATABASE}"
mongo admin << EOF
use $TEST_DATABASE
db.createUser({user: '$USER', pwd: '$PASS', roles:[{role:'dbOwner',db:'$TEST_DATABASE'}]})
EOF

echo "=> Creating an ${USER} user with a ${_word} password in MongoDB - ${DATABASE}"
mongo admin << EOF
use $DATABASE
db.createUser({user: '$USER', pwd: '$PASS', roles:[{role:'dbOwner',db:'$DATABASE'}]})
EOF

echo "=> Done!"
touch /data/db/.mongodb_password_set

echo "========================================================================"
echo "You can now connect to this MongoDB server using:"
echo ""
echo "    mongo $DATABASE -u $USER -p $PASS --host <host> --port <port>"
echo ""
echo "Please remember to change the above password as soon as possible!"
echo "========================================================================"