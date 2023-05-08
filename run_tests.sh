# Check if the number of arguments is correct
if [ $# -ne 2 ]; then
    echo "Usage: $0 <jar file name> <remote password>"
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

# Copy the jar file and nodes.txt to each of the nodes using sshpass in silent mode
while read -r line; do
    sshpass -p $2 scp $1 root@$line:~/
    sshpass -p $2 scp nodes.txt root@$line:~/
done < nodes.txt

# Run the jar file on each node passing the node's index as a parameter without waiting for the process to finish
index=0
while read -r line; do
    sshpass -p $2 ssh -n root@$line "nohup java -jar $1 $index &"
    index=$((index+1))
done < nodes.txt

exit 0
