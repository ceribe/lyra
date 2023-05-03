#The first parameter passed to this script is the name of the jar file to be tested.
#In nodes.txt there is a list of computer names, one per line, that are the nodes of the cluster.
#Script should copy the jar file and nodes.txt to each of the nodes using sshpass.
#The password for root on all the nodes is "password".
#After copying the files, the script should run the jar file on each node passing the node's index in nodes.txt as a parameter.


# Check if the number of arguments is correct
if [ $# -ne 1 ]; then
    echo "Usage: $0 <jar file name>"
    exit 1
fi

# Check if the jar file exists
if [ ! -f $1 ]; then
    echo "File $1 does not exist"
    exit 1
fi

# Check if the nodes.txt file exists
if [ ! -f nodes.txt ]; then
    echo "File nodes.txt does not exist"
    exit 1
fi

# Copy the jar file and nodes.txt to each of the nodes using sshpass in silent mode using the password "password"
while read -r line; do
    sshpass -p "password" scp $1 root@$line:~/
    sshpass -p "password" scp nodes.txt root@$line:~/
done < nodes.txt

# Run the jar file on each node passing the node's index in nodes.txt as a parameter without waiting for the process to finish (use nohup)
while read -r line; do
    sshpass -p "password" ssh -n root@$line "nohup java -jar $1 $line &"
done < nodes.txt

exit 0
