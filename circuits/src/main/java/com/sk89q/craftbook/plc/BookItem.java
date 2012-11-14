/*
    Copyright (C) 2012  Joshua Reetz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// Code from http://forums.bukkit.org/threads/simple-temp-book-api.93562/
// TODO Replace when proper Bukkit Book API exists

package com.sk89q.craftbook.plc;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.craftbukkit.inventory.CraftItemStack;

class BookItem {

    private net.minecraft.server.ItemStack item = null;
    private CraftItemStack stack = null;

    public BookItem(org.bukkit.inventory.ItemStack item) {

        if (item instanceof CraftItemStack) {
            stack = (CraftItemStack) item;
            this.item = stack.getHandle();
        } else if (item != null) {
            stack = new CraftItemStack(item);
            this.item = stack.getHandle();
        }
    }

    public String[] getPages() {

        NBTTagCompound tags = item.getTag();
        if (tags == null) return null;
        NBTTagList pages = tags.getList("pages");
        String[] pagestrings = new String[pages.size()];
        for (int i = 0; i < pages.size(); i++) {
            pagestrings[i] = pages.get(i).toString();
        }
        return pagestrings;
    }

    public String getAuthor() {

        NBTTagCompound tags = item.getTag();
        if (tags == null) return null;
        String author = tags.getString("author");
        return author;
    }

    public String getTitle() {

        NBTTagCompound tags = item.getTag();
        if (tags == null) return null;
        String title = tags.getString("title");
        return title;
    }

    public void setPages(String[] newpages) {

        NBTTagCompound tags = item.tag;
        if (tags == null) {
            tags = item.tag = new NBTTagCompound();
        }
        NBTTagList pages = new NBTTagList("pages");
        //we don't want to throw any errors if the book is blank!
        if (newpages.length == 0) {
            pages.add(new NBTTagString("1", ""));
        } else {
            for (int i = 0; i < newpages.length; i++) {
                pages.add(new NBTTagString(String.valueOf(i), newpages[i]));
            }
        }
        tags.set("pages", pages);
    }

    public void addPages(String[] newpages) {

        NBTTagCompound tags = item.tag;
        if (tags == null) {
            tags = item.tag = new NBTTagCompound();
        }
        NBTTagList pages;
        if (getPages() == null) {
            pages = new NBTTagList("pages");
        } else {
            pages = tags.getList("pages");
        }
        //we don't want to throw any errors if the book is blank!
        if (newpages.length == 0 && pages.size() == 0) {
            pages.add(new NBTTagString("1", ""));
        } else {
            for (String newpage : newpages) {
                pages.add(new NBTTagString(String.valueOf(pages.size()), newpage));
            }
        }
        tags.set("pages", pages);
    }

    public void setAuthor(String author) {

        NBTTagCompound tags = item.tag;
        if (tags == null) {
            tags = item.tag = new NBTTagCompound();
        }
        if (author != null && !author.isEmpty()) {
            tags.setString("author", author);
        }
    }

    public void setTitle(String title) {

        NBTTagCompound tags = item.tag;
        if (tags == null) {
            tags = item.tag = new NBTTagCompound();
        }
        if (title != null && !title.isEmpty()) {
            tags.setString("title", title);
        }
    }

    public org.bukkit.inventory.ItemStack getItemStack() {

        return stack;
    }

}