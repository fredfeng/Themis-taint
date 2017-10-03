echo $1 $2 $3
ant analyze -Dtarget=$1 -Dsdk=./lib/model.jar -Dcp=$2 -Dentry=$3
