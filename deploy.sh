git config --global user.email $mail
git config --global user.name $name
git remote set-url origin https://$username:$token@github.com/$username/KLU_Yemek.git
if git status --porcelain | grep -q 'list*'; then 
	git add list.json
	git add list_all.json
	git add list_raw.json
	git commit -am 'update list'
	echo $username | echo $token | git push -f origin HEAD:master
else
	echo 'No changes detected'
fi
