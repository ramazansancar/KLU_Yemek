git config --global user.email $mail
git config --global user.name $name
git remote set-url origin https://$username:$token@github.com/$username/KLU_Yemek.git
if git status --porcelain | grep -q 'list.json'; then 
	git add list.json
	git commit -am 'update list'
	echo $username | echo $token | git push -f origin HEAD:master	
else
	echo 'No changes detected'
fi
